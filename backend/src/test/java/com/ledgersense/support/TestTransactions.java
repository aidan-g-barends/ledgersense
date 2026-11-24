package com.ledgersense.support;

import com.ledgersense.domain.BankTransaction;
import com.ledgersense.factory.BankTransactionFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

// NEW: one way to make an in-memory transaction for rule tests (like TestAuth for controller tests)
public final class TestTransactions {

    private TestTransactions() {
    }

    public static BankTransaction tx(LocalDate date, String amount, UUID merchantId, UUID categoryId) {
        BankTransaction tx = BankTransactionFactory.createBankTransaction(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), date, "TEST MERCHANT", new BigDecimal(amount), null, "a".repeat(64));
        // WHY: no database in rule tests, so we give it the id the DB would have given
        ReflectionTestUtils.setField(tx, "id", UUID.randomUUID());
        tx.linkMerchant(merchantId);
        tx.categorise(categoryId);
        return tx;
    }
}