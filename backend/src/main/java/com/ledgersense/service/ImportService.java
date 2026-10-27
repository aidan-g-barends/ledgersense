package com.ledgersense.service;

import com.ledgersense.domain.*;
import com.ledgersense.factory.BankTransactionFactory;
import com.ledgersense.factory.ImportBatchFactory;
import com.ledgersense.importer.*;
import com.ledgersense.repository.BankTransactionRepository;
import com.ledgersense.repository.ImportBatchRepository;
import com.ledgersense.service.impl.IBankAccountService;
import com.ledgersense.service.impl.ICategoryRuleService;
import com.ledgersense.service.impl.IImportService;
import com.ledgersense.service.impl.IMerchantService;
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
import java.time.LocalDate;
import java.util.*;

@Service
public class ImportService implements IImportService {

    static final long MAX_BYTES = 2 * 1024 * 1024;
    static final int MAX_ERRORS_RETURNED = 100;

    private final CsvStatementParser csvStatementParser;
    private final IBankAccountService bankAccountService;
    private final ICategoryRuleService categoryRuleService;
    private final IMerchantService merchantService;                    // NEW
    private final ImportBatchRepository importBatchRepository;
    private final BankTransactionRepository bankTransactionRepository;

    public ImportService(CsvStatementParser csvStatementParser,
                         IBankAccountService bankAccountService,
                         ICategoryRuleService categoryRuleService,
                         IMerchantService merchantService,
                         ImportBatchRepository importBatchRepository,
                         BankTransactionRepository bankTransactionRepository) {
        this.csvStatementParser = csvStatementParser;
        this.bankAccountService = bankAccountService;
        this.categoryRuleService = categoryRuleService;
        this.merchantService = merchantService;
        this.importBatchRepository = importBatchRepository;
        this.bankTransactionRepository = bankTransactionRepository;
    }

    @Override
    public ParseResult preview(UUID orgId, UUID bankAccountId, MultipartFile file, ColumnMapping mapping) {
        BankAccount account = bankAccountService.read(bankAccountId, orgId);
        return parseFile(file, resolveMapping(account, mapping));
    }

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

        // STEP 1: fingerprint every row (occurrence counter keeps identical same-day rows apart)
        Map<String, Integer> occurrences = new HashMap<>();
        List<String> fingerprints = new ArrayList<>();
        for (ParsedRow row : parsed.rows()) {
            String sameRowKey = row.postedOn() + "|" + row.amount().toPlainString() + "|" + row.description().toUpperCase(Locale.ROOT);
            int occurrence = occurrences.merge(sameRowKey, 1, Integer::sum);
            fingerprints.add(TransactionFingerprint.of(
                    bankAccountId, row.postedOn(), row.amount(), row.description(), occurrence));
        }

        // STEP 2: one query for everything already imported into this account
        Set<String> alreadyImported = bankTransactionRepository.findExistingFingerprints(bankAccountId, fingerprints);

        // STEP 3: keep only new rows, and collect each new row's merchant name + earliest date
        List<ParsedRow> newRows = new ArrayList<>();
        List<String> newFingerprints = new ArrayList<>();
        Map<String, LocalDate> firstSeenByMerchant = new HashMap<>();
        int duplicates = 0;
        for (int i = 0; i < parsed.rows().size(); i++) {
            ParsedRow row = parsed.rows().get(i);
            if (alreadyImported.contains(fingerprints.get(i))) {
                duplicates++;
                continue;
            }
            newRows.add(row);
            newFingerprints.add(fingerprints.get(i));
            String merchantName = MerchantNameNormalizer.normalize(row.description());
            if (merchantName != null) {
                // NEW: keep the EARLIEST date we've seen this merchant in this file
                firstSeenByMerchant.merge(merchantName, row.postedOn(), (a, b) -> a.isBefore(b) ? a : b);
            }
        }

        // STEP 4 (NEW): find or create every merchant in bulk, and load rules once
        Map<String, Merchant> merchants = merchantService.findOrCreate(orgId, firstSeenByMerchant);
        List<CategoryRule> rules = categoryRuleService.readAll(orgId);

        // STEP 5: build the transactions
        List<BankTransaction> toSave = new ArrayList<>();
        int rejected = 0;
        for (int i = 0; i < newRows.size(); i++) {
            ParsedRow row = newRows.get(i);
            BankTransaction tx = BankTransactionFactory.createBankTransaction(orgId, bankAccountId, batch.getId(),
                    row.postedOn(), row.description(), row.amount(), row.balance(), newFingerprints.get(i));
            if (tx == null) {
                rejected++;
                continue;
            }

            // NEW: category priority. 1) what the user taught us for this merchant, 2) keyword rules.
            UUID categoryId = null;
            Merchant merchant = merchants.get(MerchantNameNormalizer.normalize(row.description()));
            if (merchant != null) {
                tx.linkMerchant(merchant.getId());
                categoryId = merchant.getCategoryId();
            }
            if (categoryId == null) {
                categoryId = CategoryMatcher.match(row.description(), rules);
            }
            tx.categorise(categoryId);
            toSave.add(tx);
        }

        // STEP 6: save in one go, record the stats on the batch (dirty checking saves it)
        bankTransactionRepository.saveAll(toSave);
        int total = parsed.rows().size() + parsed.errors().size();
        batch.complete(total, toSave.size(), total - toSave.size(), Instant.now());

        return new ImportResult(batch.getId(), batch.getFileName(), total, toSave.size(), duplicates,
                parsed.errors().size() + rejected,
                parsed.errors().stream().limit(MAX_ERRORS_RETURNED).toList());
    }

    // NEW: newest imports first, this org only
    @Override
    @Transactional(readOnly = true)
    public List<ImportBatch> history(UUID orgId) {
        return importBatchRepository.findAllByOrgIdOrderByCreatedAtDesc(orgId);
    }

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