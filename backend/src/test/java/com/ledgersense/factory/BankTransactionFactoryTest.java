package com.ledgersense.factory;

import com.ledgersense.domain.BankTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BankTransactionFactoryTest {

    private final UUID orgId = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();
    private final UUID batchId = UUID.randomUUID();
    private final String fingerprint = "a".repeat(64);
    private final LocalDate yesterday = LocalDate.now().minusDays(1);

    @Test
    void createBankTransaction() {
        BankTransaction tx = BankTransactionFactory.createBankTransaction(orgId, accountId, batchId,
                yesterday, "  POS PURCHASE   WOOLWORTHS ", new BigDecimal("-450.5"), null, fingerprint);

        assertNotNull(tx);
        assertEquals("POS PURCHASE WOOLWORTHS", tx.getDescription());
        assertEquals(new BigDecimal("-450.50"), tx.getAmount());
        assertTrue(tx.isMoneyOut());
    }

    @Test
    void createBankTransaction_withFractionOfCent() {
        BankTransaction tx = BankTransactionFactory.createBankTransaction(orgId, accountId, batchId,
                yesterday, "WOOLWORTHS", new BigDecimal("-450.505"), null, fingerprint);

        assertNull(tx);
    }

    @Test
    void createBankTransaction_withFutureDate() {
        BankTransaction tx = BankTransactionFactory.createBankTransaction(orgId, accountId, batchId,
                LocalDate.now().plusDays(5), "WOOLWORTHS", new BigDecimal("-450.50"), null, fingerprint);

        assertNull(tx);
    }
}
