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
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// NEW: "your R349 subscription is R449 this month"
@Component
public class PriceIncreaseRule implements DetectionRule {

    static final BigDecimal MIN_INCREASE = new BigDecimal("10.00"); // ignore tiny wobbles like R0.50
    static final BigDecimal MIN_PERCENT = new BigDecimal("5");
    // WHY: subscriptions bill on (about) the same day every month. Two random shop visits a month apart aren't a "price".
    static final int BILLING_DAY_TOLERANCE = 3;

    @Override
    public FlagRule rule() {
        return FlagRule.PRICE_INCREASE;
    }

    @Override
    public List<AnomalyFlag> detect(UUID orgId, List<BankTransaction> newTransactions, List<BankTransaction> window) {
        List<AnomalyFlag> flags = new ArrayList<>();
        for (BankTransaction tx : newTransactions) {
            if (!tx.isMoneyOut()) {
                continue;
            }
            BankTransaction previous = lastMonthsCharge(tx, window);
            if (previous == null
                    || Math.abs(tx.getPostedOn().getDayOfMonth() - previous.getPostedOn().getDayOfMonth()) > BILLING_DAY_TOLERANCE) {
                continue;
            }

            BigDecimal now = tx.getAmount().abs();
            BigDecimal before = previous.getAmount().abs();
            BigDecimal increase = now.subtract(before);
            if (increase.compareTo(MIN_INCREASE) < 0) {
                continue;
            }
            // increase / before * 100, rounded to 1 decimal: 100 / 349 = 28.7%
            BigDecimal percent = increase.multiply(BigDecimal.valueOf(100)).divide(before, 1, RoundingMode.HALF_UP);
            if (percent.compareTo(MIN_PERCENT) < 0) {
                continue;
            }

            AnomalyFlag flag = AnomalyFlagFactory.createAnomalyFlag(orgId, tx.getId(), FlagRule.PRICE_INCREASE,
                    FlagSeverity.WARNING, "Subscription price went up", "This recurring charge is higher than last month.",
                    List.of(new FlagEvidence("Last month",
                                    DisplayFormat.money(before) + " on " + DisplayFormat.date(previous.getPostedOn())),
                            new FlagEvidence("This month", DisplayFormat.money(now)),
                            new FlagEvidence("Change",
                                    "+" + DisplayFormat.money(increase) + " (+" + percent.toPlainString() + "%)")));
            if (flag != null) {
                flags.add(flag);
            }
        }
        return flags;
    }

    // The same merchant's LATEST money-out charge in the previous calendar month (null if none)
    private BankTransaction lastMonthsCharge(BankTransaction tx, List<BankTransaction> window) {
        YearMonth previousMonth = YearMonth.from(tx.getPostedOn()).minusMonths(1);
        String who = MerchantKey.of(tx);
        BankTransaction latest = null;
        for (BankTransaction other : window) { // oldest first, so the last match is the latest
            if (other.isMoneyOut() && MerchantKey.of(other).equals(who)
                    && YearMonth.from(other.getPostedOn()).equals(previousMonth)) {
                latest = other;
            }
        }
        return latest;
    }
}