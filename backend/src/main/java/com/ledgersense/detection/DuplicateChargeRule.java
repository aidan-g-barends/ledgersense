package com.ledgersense.detection;

import com.ledgersense.domain.AnomalyFlag;
import com.ledgersense.domain.BankTransaction;
import com.ledgersense.domain.FlagEvidence;
import com.ledgersense.domain.FlagRule;
import com.ledgersense.domain.FlagSeverity;
import com.ledgersense.factory.AnomalyFlagFactory;
import com.ledgersense.util.DisplayFormat;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;

@Component
public class DuplicateChargeRule implements DetectionRule {

    // WHY a minimum: two R38 coffees on one day is normal life, not a billing error. Below this = noise.
    static final BigDecimal MIN_AMOUNT = new BigDecimal("50.00");

    @Override
    public FlagRule rule() {
        return FlagRule.DUPLICATE_CHARGE;
    }

    @Override
    public List<AnomalyFlag> detect(UUID orgId, List<BankTransaction> newTransactions, List<BankTransaction> window) {
        Set<UUID> newIds = new HashSet<>();
        for (BankTransaction tx : newTransactions) {
            newIds.add(tx.getId());
        }

        // LinkedHashMap keeps insertion order, so the "first" in each group really is the earliest
        Map<String, List<BankTransaction>> sameCharge = new LinkedHashMap<>();
        for (BankTransaction tx : window) {
            if (!tx.isMoneyOut() || tx.getAmount().abs().compareTo(MIN_AMOUNT) < 0) {
                continue;
            }
            String key = MerchantKey.of(tx) + "|" + tx.getPostedOn() + "|" + tx.getAmount().toPlainString(); // CHANGED
            sameCharge.computeIfAbsent(key, k -> new ArrayList<>()).add(tx);
        }

        List<AnomalyFlag> flags = new ArrayList<>();
        for (List<BankTransaction> group : sameCharge.values()) {
            if (group.size() < 2) {
                continue;
            }
            boolean recurring = chargedInEarlierMonth(group.get(0), window);
            for (int i = 1; i < group.size(); i++) { // i = 1: skip the first, it's the real charge
                BankTransaction copy = group.get(i);
                if (!newIds.contains(copy.getId())) {
                    continue; // already in the DB before this import, so it was checked back then
                }
                AnomalyFlag flag = buildFlag(orgId, copy, group.size(), recurring);
                if (flag != null) {
                    flags.add(flag);
                }
            }
        }
        return flags;
    }

    // Same merchant + same amount in an EARLIER month = a regular bill (subscription, hosting, rent)
    private boolean chargedInEarlierMonth(BankTransaction charge, List<BankTransaction> window) {
        YearMonth month = YearMonth.from(charge.getPostedOn());
        String who = MerchantKey.of(charge); // CHANGED
        for (BankTransaction tx : window) {
            if (MerchantKey.of(tx).equals(who) // CHANGED
                    && tx.getAmount().compareTo(charge.getAmount()) == 0 // compareTo: 299.0 equals 299.00
                    && YearMonth.from(tx.getPostedOn()).isBefore(month)) {
                return true;
            }
        }
        return false;
    }

    private AnomalyFlag buildFlag(UUID orgId, BankTransaction copy, int count, boolean recurring) {
        List<FlagEvidence> evidence = new ArrayList<>();
        if (recurring) {
            evidence.add(new FlagEvidence("Usual pattern", "1 charge per month"));
        }
        evidence.add(new FlagEvidence("Detected", count + " charges on " + DisplayFormat.date(copy.getPostedOn())));
        evidence.add(new FlagEvidence("Each charge", DisplayFormat.money(copy.getAmount())));

        String explanation = recurring
                ? "Charged " + count + " times on the same day for the same amount. This merchant usually bills once a month."
                : "Charged " + count + " times on the same day for exactly the same amount.";

        return AnomalyFlagFactory.createAnomalyFlag(orgId, copy.getId(), FlagRule.DUPLICATE_CHARGE,
                recurring ? FlagSeverity.CRITICAL : FlagSeverity.WARNING,
                "Possible duplicate charge", explanation, evidence);
    }
}