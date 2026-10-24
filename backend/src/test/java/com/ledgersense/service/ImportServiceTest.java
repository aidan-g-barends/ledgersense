package com.ledgersense.service;

import com.ledgersense.exception.ResourceNotFoundException;
import com.ledgersense.importer.ColumnMapping;
import com.ledgersense.importer.CsvStatementParser;
import com.ledgersense.importer.ParseResult;
import com.ledgersense.service.impl.IBankAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

    @Mock
    private IBankAccountService bankAccountService;

    private ImportService importService;
    private final UUID orgId = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();
    private final ColumnMapping mapping = new ColumnMapping(',', 0, true, "uuuu/MM/dd",
            0, 1, 2, null, null, null);

    @BeforeEach
    void setUp() {
        importService = new ImportService(new CsvStatementParser(), bankAccountService);
    }

    private MockMultipartFile csv(String name, String content) {
        return new MockMultipartFile("file", name, "text/csv", content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void preview() {
        MockMultipartFile file = csv("statement.csv", """
                Date,Description,Amount
                2026/10/01,WOOLWORTHS,-450.50
                2026/10/02,SPAR,-99.99
                """);

        ParseResult result = importService.preview(orgId, accountId, file, mapping);

        assertEquals(2, result.rows().size());
    }

    @Test
    void preview_whenAccountFromOtherOrg() {
        when(bankAccountService.read(accountId, orgId))
                .thenThrow(new ResourceNotFoundException("Bank account not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> importService.preview(orgId, accountId, csv("statement.csv", "x"), mapping));
    }

    @Test
    void preview_whenNotCsv() {
        assertThrows(IllegalArgumentException.class,
                () -> importService.preview(orgId, accountId, csv("statement.xlsx", "x"), mapping));
    }
}