package com.ledgersense.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionFingerprintTest {

    private final UUID accountId = UUID.randomUUID();
    private final LocalDate date = LocalDate.of(2026, 10, 19);

    @Test
    void sameRow_sameFingerprint() {
        String a = TransactionFingerprint.of(accountId, date, new BigDecimal("-35.0"), "WOOLWORTHS  CPT", 1);
        String b = TransactionFingerprint.of(accountId, date, new BigDecimal("-35.00"), "woolworths cpt", 1);

        assertEquals(a, b);
    }

    @Test
    void secondIdenticalRow_differentFingerprint() {
        String first = TransactionFingerprint.of(accountId, date, new BigDecimal("-35.00"), "COFFEE", 1);
        String second = TransactionFingerprint.of(accountId, date, new BigDecimal("-35.00"), "COFFEE", 2);

        assertNotEquals(first, second);
    }
}
