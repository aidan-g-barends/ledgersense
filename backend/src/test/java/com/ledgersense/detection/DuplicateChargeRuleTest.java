package com.ledgersense.detection;

import com.ledgersense.domain.AnomalyFlag;
import com.ledgersense.domain.BankTransaction;
import com.ledgersense.domain.FlagSeverity;
import com.ledgersense.factory.BankTransactionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateChargeRuleTest {

    private final DuplicateChargeRule rule = new DuplicateChargeRule();
    private final UUID orgId = UUID.randomUUID();
    private final LocalDate day = LocalDate.now().minusDays(1);

    private BankTransaction tx(LocalDate date, String amount) {
        BankTransaction tx = BankTransactionFactory.createBankTransaction(orgId, UUID.randomUUID(), UUID.randomUUID(),
                date, "CLOUD HOST LTD", new BigDecimal(amount), null, "a".repeat(64));
        // WHY: there's no database in this test, so we give it the id the DB would have given
        ReflectionTestUtils.setField(tx, "id", UUID.randomUUID());
        return tx;
    }

    @Test
    void detect_flagsTheSecondCharge() {
        BankTransaction first = tx(day, "-299.00");
        BankTransaction second = tx(day, "-299.00");

        List<AnomalyFlag> flags = rule.detect(orgId, List.of(first, second), List.of(first, second));

        assertEquals(1, flags.size());
        assertEquals(second.getId(), flags.get(0).getTransactionId());
        assertEquals(FlagSeverity.WARNING, flags.get(0).getSeverity());
    }

    @Test
    void detect_recurringBillIsCritical() {
        BankTransaction lastMonth = tx(day.minusMonths(1), "-299.00");
        BankTransaction first = tx(day, "-299.00");
        BankTransaction second = tx(day, "-299.00");

        List<AnomalyFlag> flags = rule.detect(orgId, List.of(first, second), List.of(lastMonth, first, second));

        assertEquals(FlagSeverity.CRITICAL, flags.get(0).getSeverity());
    }

    @Test
    void detect_ignoresSmallAmounts() {
        BankTransaction first = tx(day, "-38.50");
        BankTransaction second = tx(day, "-38.50");

        assertTrue(rule.detect(orgId, List.of(first, second), List.of(first, second)).isEmpty());
    }
}