package com.ledgersense.detection;

import com.ledgersense.domain.AnomalyFlag;
import com.ledgersense.domain.BankTransaction;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.ledgersense.support.TestTransactions.tx;
import static org.junit.jupiter.api.Assertions.*;

class CategoryOutlierRuleTest {

    private final CategoryOutlierRule rule = new CategoryOutlierRule();
    private final UUID restaurants = UUID.randomUUID();
    private final LocalDate day = LocalDate.of(2026, 10, 23);

    private List<BankTransaction> history(String... amounts) {
        List<BankTransaction> list = new ArrayList<>();
        for (String amount : amounts) {
            list.add(tx(day.minusDays(list.size() + 1), amount, null, restaurants));
        }
        return list;
    }

    @Test
    void detect_flagsHugeRestaurantBill() {
        BankTransaction bigBill = tx(day, "-2850.00", null, restaurants);
        List<BankTransaction> window = history("-100.00", "-110.00", "-120.00", "-130.00", "-150.00");
        window.add(bigBill);

        List<AnomalyFlag> flags = rule.detect(UUID.randomUUID(), List.of(bigBill), window);

        assertEquals(1, flags.size());
        assertEquals(bigBill.getId(), flags.get(0).getTransactionId());
    }

    @Test
    void detect_needsEnoughHistory() {
        BankTransaction bigBill = tx(day, "-2850.00", null, restaurants);
        List<BankTransaction> window = history("-100.00", "-120.00", "-150.00");
        window.add(bigBill);

        assertTrue(rule.detect(UUID.randomUUID(), List.of(bigBill), window).isEmpty());
    }
}