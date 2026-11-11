package com.ledgersense.detection;

import com.ledgersense.domain.AnomalyFlag;
import com.ledgersense.domain.BankTransaction;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.ledgersense.support.TestTransactions.tx;
import static org.junit.jupiter.api.Assertions.*;

class PriceIncreaseRuleTest {

    private final PriceIncreaseRule rule = new PriceIncreaseRule();
    private final UUID designTools = UUID.randomUUID();

    @Test
    void detect_flagsHigherSubscription() {
        BankTransaction september = tx(LocalDate.of(2026, 9, 22), "-349.00", designTools, null);
        BankTransaction october = tx(LocalDate.of(2026, 10, 22), "-449.00", designTools, null);

        List<AnomalyFlag> flags = rule.detect(UUID.randomUUID(), List.of(october), List.of(september, october));

        assertEquals(1, flags.size());
        assertEquals("+R 100,00 (+28.7%)", flags.get(0).getEvidence().get(2).getValue());
    }

    @Test
    void detect_ignoresSamePrice() {
        BankTransaction september = tx(LocalDate.of(2026, 9, 22), "-349.00", designTools, null);
        BankTransaction october = tx(LocalDate.of(2026, 10, 22), "-349.00", designTools, null);

        assertTrue(rule.detect(UUID.randomUUID(), List.of(october), List.of(september, october)).isEmpty());
    }
}