package com.ledgersense.service;

import com.ledgersense.domain.Bank;
import com.ledgersense.exception.ResourceNotFoundException;
import com.ledgersense.factory.BankAccountFactory;
import com.ledgersense.importer.ColumnMapping;
import com.ledgersense.importer.CsvStatementParser;
import com.ledgersense.importer.ParseResult;
import com.ledgersense.repository.BankTransactionRepository;
import com.ledgersense.repository.ImportBatchRepository;
import com.ledgersense.service.impl.IBankAccountService;
import com.ledgersense.service.impl.ICategoryRuleService;
import com.ledgersense.service.impl.IMerchantService;
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

    @Mock
    private ICategoryRuleService categoryRuleService;           // NEW

    @Mock
    private ImportBatchRepository importBatchRepository;        // NEW

    @Mock
    private BankTransactionRepository bankTransactionRepository;

    @Mock
    private IMerchantService merchantService;

    private ImportService importService;
    private final UUID orgId = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();
    private final ColumnMapping mapping = new ColumnMapping(',', 0, true, "uuuu/MM/dd",
            0, 1, 2, null, null, null);

    @BeforeEach
    @BeforeEach
    void setUp() {
        importService = new ImportService(new CsvStatementParser(), bankAccountService,
                categoryRuleService, merchantService, importBatchRepository, bankTransactionRepository); // CHANGED
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

    // NEW: bank has no preset and no mapping was sent, so we ask the user to map columns
    @Test
    void preview_whenNoPresetAndNoMapping() {
        when(bankAccountService.read(accountId, orgId))
                .thenReturn(BankAccountFactory.create(orgId, "Other", Bank.OTHER, "1111", "ZAR"));

        assertThrows(IllegalArgumentException.class,
                () -> importService.preview(orgId, accountId, csv("statement.csv", "x"), null));
    }

    // NEW: nothing valid to save = clear 400, and no empty batch gets created
    @Test
    void commit_whenNoValidRows() {
        MockMultipartFile file = csv("statement.csv", """
                Date,Description,Amount
                2026/13/45,BROKEN,-1.00
                """);

        assertThrows(IllegalArgumentException.class,
                () -> importService.commit(orgId, accountId, file, mapping));
    }
}