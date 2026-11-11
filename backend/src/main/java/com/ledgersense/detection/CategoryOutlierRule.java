package com.ledgersense.detection;

import com.ledgersense.domain.AnomalyFlag;
import com.ledgersense.domain.BankTransaction;
import com.ledgersense.domain.FlagEvidence;
import com.ledgersense.domain.FlagRule;
import com.ledgersense.domain.FlagSeverity;
import com.ledgersense.factory.AnomalyFlagFactory;
import com.ledgersense.util.DisplayFormat;
import com.ledgersense.util.RobustStats;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

// NEW: "you normally spend ~R300 at restaurants, this one was R2 850"
@Component
public class CategoryOutlierRule implements DetectionRule {

    static final int MIN_HISTORY = 5;                              // fewer past charges = no reliable "normal" yet
    static final double THRESHOLD = 3.5;                           // standard robust z-score cut-off
    static final BigDecimal MIN_AMOUNT = new BigDecimal("100.00"); // a "big" R60 coffee isn't worth a flag

    @Override
    public FlagRule rule() {
        return FlagRule.CATEGORY_OUTLIER;
    }

    @Override
    public List<AnomalyFlag> detect(UUID orgId, List<BankTransaction> newTransactions, List<BankTransaction> window) {
        // Every money-out charge in the window, grouped by category
        Map<UUID, List<BankTransaction>> byCategory = new HashMap<>();
        for (BankTransaction tx : window) {
            if (tx.isMoneyOut() && tx.getCategoryId() != null) {
                byCategory.computeIfAbsent(tx.getCategoryId(), k -> new ArrayList<>()).add(tx);
            }
        }

        List<AnomalyFlag> flags = new ArrayList<>();
        for (BankTransaction tx : newTransactions) {
            if (!tx.isMoneyOut() || tx.getCategoryId() == null || tx.getAmount().abs().compareTo(MIN_AMOUNT) < 0) {
                continue;
            }

            // "Normal" = every OTHER charge in this category (leave-one-out, so it isn't compared to itself)
            double[] others = byCategory.getOrDefault(tx.getCategoryId(), List.of()).stream()
                    .filter(other -> !other.getId().equals(tx.getId()))
                    .mapToDouble(other -> other.getAmount().abs().doubleValue())
                    .toArray();
            if (others.length < MIN_HISTORY) {
                continue;
            }

            double median = RobustStats.median(others);
            double mad = RobustStats.mad(others);
            double amount = tx.getAmount().abs().doubleValue();
            // WHY skip mad == 0: every past charge was identical, spread is 0, the score would divide by zero
            // WHY skip amount <= median: only unusually LARGE spend matters here
            if (mad == 0 || amount <= median) {
                continue;
            }

            double score = RobustStats.robustZ(amount, median, mad);
            if (score > THRESHOLD) {
                AnomalyFlag flag = AnomalyFlagFactory.createAnomalyFlag(orgId, tx.getId(), FlagRule.CATEGORY_OUTLIER,
                        FlagSeverity.WARNING, "Unusually large payment",
                        "Much higher than what you normally spend in this category.",
                        List.of(new FlagEvidence("Typical charge", "about " + DisplayFormat.money(BigDecimal.valueOf(median))),
                                new FlagEvidence("This charge", DisplayFormat.money(tx.getAmount())),
                                new FlagEvidence("Outlier score",
                                        String.format(Locale.ROOT, "%.1f (flagged above %.1f)", score, THRESHOLD))));
                if (flag != null) {
                    flags.add(flag);
                }
            }
        }
        return flags;
    }
}