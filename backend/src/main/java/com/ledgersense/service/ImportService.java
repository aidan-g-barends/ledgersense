package com.ledgersense.service;

import com.ledgersense.importer.ColumnMapping;
import com.ledgersense.importer.CsvStatementParser;
import com.ledgersense.importer.ParseResult;
import com.ledgersense.service.impl.IBankAccountService;
import com.ledgersense.service.impl.IImportService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

@Service
public class ImportService implements IImportService {

    static final long MAX_BYTES = 2 * 1024 * 1024; // 2MB, same as application.yaml

    private final CsvStatementParser csvStatementParser;
    private final IBankAccountService bankAccountService;

    public ImportService(CsvStatementParser csvStatementParser, IBankAccountService bankAccountService) {
        this.csvStatementParser = csvStatementParser;
        this.bankAccountService = bankAccountService;
    }

    @Override
    public ParseResult preview(UUID orgId, UUID bankAccountId, MultipartFile file, ColumnMapping mapping) {
        // SECURITY: bankAccountId came from the client. 404 if it's not THIS org's account.
        bankAccountService.read(bankAccountId, orgId);

        validateFile(file);
        String text = decode(readBytes(file));

        // WHY: a text CSV never contains a NUL byte. Binary files (xlsx, pdf, images) almost always do.
        if (text.indexOf('\u0000') >= 0) {
            throw new IllegalArgumentException("That doesn't look like a CSV file. Export your statement as CSV and try again.");
        }

        try {
            return csvStatementParser.parse(new StringReader(text), mapping);
        } catch (IOException ex) {
            // StringReader can't really fail, but the signature says it might
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
        // WHY check the NAME, not the content type: the browser decides the content type,
        // so it can say anything. The name check is a first filter; the NUL-byte check is the real one.
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

    // NEW: try strict UTF-8 first. If the bytes aren't valid UTF-8, fall back to Windows-1252,
    // which older bank exports and Excel on Windows still use.
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