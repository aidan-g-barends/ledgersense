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
import java.util.*;

// NEW: "you've never paid this merchant before". INFO only: new isn't bad, just worth a glance.
@Component
public class NewMerchantRule implements DetectionRule {

    static final BigDecimal MIN_AMOUNT = new BigDecimal("500.00"); // a first R40 coffee somewhere new isn't news

    @Override
    public FlagRule rule() {
        return FlagRule.NEW_MERCHANT;
    }

    @Override
    public List<AnomalyFlag> detect(UUID orgId, List<BankTransaction> newTransactions, List<BankTransaction> window) {
        Set<UUID> newIds = new HashSet<>();
        for (BankTransaction tx : newTransactions) {
            newIds.add(tx.getId());
        }

        // Merchants paid BEFORE this import (within the window)
        Set<String> seenBefore = new HashSet<>();
        for (BankTransaction tx : window) {
            if (!newIds.contains(tx.getId())) {
                seenBefore.add(MerchantKey.of(tx));
            }
        }
        // WHY: on your very FIRST import, every merchant is "new". Flagging them all = noise. Wait for history.
        if (seenBefore.isEmpty()) {
            return List.of();
        }

        Set<String> alreadyHandled = new HashSet<>(); // one flag per merchant, even if paid 3 times in this file
        List<AnomalyFlag> flags = new ArrayList<>();
        for (BankTransaction tx : window) { // window is oldest first, so the FIRST payment is the one we see first
            if (!newIds.contains(tx.getId()) || !tx.isMoneyOut() || tx.getMerchantId() == null) {
                continue;
            }
            String key = MerchantKey.of(tx);
            if (seenBefore.contains(key) || !alreadyHandled.add(key)) { // add() returns false if it was already there
                continue;
            }
            if (tx.getAmount().abs().compareTo(MIN_AMOUNT) < 0) {
                continue;
            }
            AnomalyFlag flag = AnomalyFlagFactory.createAnomalyFlag(orgId, tx.getId(), FlagRule.NEW_MERCHANT,
                    FlagSeverity.INFO, "First payment to a new merchant", "You haven't paid this merchant before.",
                    List.of(new FlagEvidence("First seen", DisplayFormat.date(tx.getPostedOn())),
                            new FlagEvidence("Amount", DisplayFormat.money(tx.getAmount()))));
            if (flag != null) {
                flags.add(flag);
            }
        }
        return flags;
    }
}