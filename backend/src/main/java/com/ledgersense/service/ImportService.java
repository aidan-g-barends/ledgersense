package com.ledgersense.service;

import com.ledgersense.domain.BankAccount;
import com.ledgersense.domain.BankTransaction;
import com.ledgersense.domain.CategoryRule;
import com.ledgersense.domain.ImportBatch;
import com.ledgersense.factory.BankTransactionFactory;
import com.ledgersense.factory.ImportBatchFactory;
import com.ledgersense.importer.*;
import com.ledgersense.repository.BankTransactionRepository;
import com.ledgersense.repository.ImportBatchRepository;
import com.ledgersense.service.impl.IBankAccountService;
import com.ledgersense.service.impl.ICategoryRuleService;
import com.ledgersense.service.impl.IImportService;
import com.ledgersense.util.TransactionFingerprint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
public class ImportService implements IImportService {

    static final long MAX_BYTES = 2 * 1024 * 1024;
    static final int MAX_ERRORS_RETURNED = 100;

    private final CsvStatementParser csvStatementParser;
    private final IBankAccountService bankAccountService;
    private final ICategoryRuleService categoryRuleService;          // NEW
    private final ImportBatchRepository importBatchRepository;       // NEW
    private final BankTransactionRepository bankTransactionRepository; // NEW

    public ImportService(CsvStatementParser csvStatementParser,
                         IBankAccountService bankAccountService,
                         ICategoryRuleService categoryRuleService,
                         ImportBatchRepository importBatchRepository,
                         BankTransactionRepository bankTransactionRepository) {
        this.csvStatementParser = csvStatementParser;
        this.bankAccountService = bankAccountService;
        this.categoryRuleService = categoryRuleService;
        this.importBatchRepository = importBatchRepository;
        this.bankTransactionRepository = bankTransactionRepository;
    }

    @Override
    public ParseResult preview(UUID orgId, UUID bankAccountId, MultipartFile file, ColumnMapping mapping) {
        BankAccount account = bankAccountService.read(bankAccountId, orgId); // 404 if not this org's
        return parseFile(file, resolveMapping(account, mapping));
    }

    // NEW: ONE transaction. If anything fails, the batch AND every row roll back together.
    @Override
    @Transactional
    public ImportResult commit(UUID orgId, UUID bankAccountId, MultipartFile file, ColumnMapping mapping) {
        BankAccount account = bankAccountService.read(bankAccountId, orgId);
        ParseResult parsed = parseFile(file, resolveMapping(account, mapping));
        if (parsed.rows().isEmpty()) {
            throw new IllegalArgumentException("No valid rows to import. Check the preview for errors.");
        }

        ImportBatch batch = ImportBatchFactory.createImportBatch(orgId, bankAccountId, file.getOriginalFilename());
        if (batch == null) {
            throw new IllegalArgumentException("Invalid file name");
        }
        batch = importBatchRepository.save(batch);

        // STEP 1: fingerprint every row.
        // WHY the occurrence counter: two identical R38.50 coffees on the same day are 2 real purchases.
        // The 1st gets occurrence 1, the 2nd gets 2, so both are kept. Re-import the same file and they
        // get 1 and 2 again, so both are recognised as duplicates.
        Map<String, Integer> occurrences = new HashMap<>();
        List<String> fingerprints = new ArrayList<>();
        for (ParsedRow row : parsed.rows()) {
            String sameRowKey = row.postedOn() + "|" + row.amount().toPlainString() + "|" + row.description().toUpperCase(Locale.ROOT);
            int occurrence = occurrences.merge(sameRowKey, 1, Integer::sum); // add 1, starting at 1
            fingerprints.add(TransactionFingerprint.of(
                    bankAccountId, row.postedOn(), row.amount(), row.description(), occurrence));
        }

        // STEP 2: ONE query for everything already imported into this account
        Set<String> alreadyImported = bankTransactionRepository.findExistingFingerprints(bankAccountId, fingerprints);

        // STEP 3: load the org's rules ONCE, then match every row in memory
        List<CategoryRule> rules = categoryRuleService.readAll(orgId);

        List<BankTransaction> toSave = new ArrayList<>();
        int duplicates = 0;
        int rejected = 0;
        for (int i = 0; i < parsed.rows().size(); i++) {
            ParsedRow row = parsed.rows().get(i);
            String fingerprint = fingerprints.get(i);

            if (alreadyImported.contains(fingerprint)) {
                duplicates++;
                continue;
            }
            BankTransaction tx = BankTransactionFactory.createBankTransaction(orgId, bankAccountId, batch.getId(),
                    row.postedOn(), row.description(), row.amount(), row.balance(), fingerprint);
            if (tx == null) {
                rejected++; // e.g. a future date: the factory said no
                continue;
            }
            tx.categorise(CategoryMatcher.match(row.description(), rules)); // null = uncategorised
            toSave.add(tx);
        }

        // STEP 4: save in one go, then record the stats on the batch (dirty checking saves it)
        bankTransactionRepository.saveAll(toSave);
        int total = parsed.rows().size() + parsed.errors().size();
        batch.complete(total, toSave.size(), total - toSave.size(), Instant.now());

        return new ImportResult(batch.getId(), batch.getFileName(), total, toSave.size(), duplicates,
                parsed.errors().size() + rejected,
                parsed.errors().stream().limit(MAX_ERRORS_RETURNED).toList());
    }

    // NEW: use the mapping the client sent, else the bank's preset, else ask for one
    private ColumnMapping resolveMapping(BankAccount account, ColumnMapping mapping) {
        if (mapping != null) {
            return mapping;
        }
        ColumnMapping preset = BankCsvPresets.forBank(account.getBank());
        if (preset == null) {
            throw new IllegalArgumentException("We don't know this bank's CSV layout yet. Please map the columns.");
        }
        return preset;
    }

    private ParseResult parseFile(MultipartFile file, ColumnMapping mapping) {
        validateFile(file);
        String text = decode(readBytes(file));
        // A text CSV never contains a NUL byte. Binary files (xlsx, pdf, images) almost always do.
        if (text.indexOf('\u0000') >= 0) {
            throw new IllegalArgumentException("That doesn't look like a CSV file. Export your statement as CSV and try again.");
        }
        try {
            return csvStatementParser.parse(new StringReader(text), mapping);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Choose a CSV file to upload.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("That file is too big. The limit is 2MB per statement.");
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new IllegalArgumentException("Only .csv files can be imported.");
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not read the uploaded file", ex);
        }
    }

    // Strict UTF-8 first; if the bytes aren't valid UTF-8, fall back to Windows-1252
    private String decode(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException ex) {
            return new String(bytes, Charset.forName("windows-1252"));
        }
    }
}