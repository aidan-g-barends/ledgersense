package com.ledgersense.detection;

import com.ledgersense.domain.AnomalyFlag;
import com.ledgersense.domain.BankTransaction;
import com.ledgersense.domain.FlagSeverity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.ledgersense.support.TestTransactions.tx;
import static org.junit.jupiter.api.Assertions.*;

class NewMerchantRuleTest {

    private final NewMerchantRule rule = new NewMerchantRule();

    @Test
    void detect_flagsFirstPaymentToNewMerchant() {
        BankTransaction oldShop = tx(LocalDate.of(2026, 9, 10), "-300.00", UUID.randomUUID(), null);
        BankTransaction seaview = tx(LocalDate.of(2026, 10, 23), "-2850.00", UUID.randomUUID(), null);

        List<AnomalyFlag> flags = rule.detect(UUID.randomUUID(), List.of(seaview), List.of(oldShop, seaview));

        assertEquals(1, flags.size());
        assertEquals(FlagSeverity.INFO, flags.get(0).getSeverity());
    }

    @Test
    void detect_staysQuietOnFirstEverImport() {
        BankTransaction seaview = tx(LocalDate.of(2026, 10, 23), "-2850.00", UUID.randomUUID(), null);

        assertTrue(rule.detect(UUID.randomUUID(), List.of(seaview), List.of(seaview)).isEmpty());
    }
}