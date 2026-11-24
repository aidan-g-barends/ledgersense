package com.ledgersense.insights;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

// NEW: everything the Overview page needs, in one object
public record InsightsSummary(YearMonth month, BigDecimal moneyIn, BigDecimal moneyOut, BigDecimal net,
                              BigDecimal moneyOutChangePct, long transactionCount,
                              List<MonthTotal> months, List<CategorySpend> topCategories) {

    // A brand-new org: zero everything. The frontend shows its "import your first CSV" screen.
    public static InsightsSummary empty(YearMonth month) {
        return new InsightsSummary(month, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null, 0, List.of(), List.of());
    }
}