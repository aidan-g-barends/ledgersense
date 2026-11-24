package com.ledgersense.dto;

import com.ledgersense.insights.InsightsSummary;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// NEW: matches InsightsSummary in the frontend's types.ts exactly.
// WHY months as String: YearMonth.toString() is always "2026-10", so the JSON can't depend on Jackson settings.
public record InsightsSummaryResponse(String month, BigDecimal moneyIn, BigDecimal moneyOut, BigDecimal net,
                                      BigDecimal moneyOutChangePct, long transactionCount,
                                      List<MonthItem> months, List<CategoryItem> topCategories) {

    public record MonthItem(String month, BigDecimal moneyIn, BigDecimal moneyOut) {
    }

    public record CategoryItem(UUID categoryId, String name, BigDecimal amount) {
    }

    public static InsightsSummaryResponse from(InsightsSummary s) {
        return new InsightsSummaryResponse(s.month().toString(), s.moneyIn(), s.moneyOut(), s.net(),
                s.moneyOutChangePct(), s.transactionCount(),
                s.months().stream().map(m -> new MonthItem(m.month().toString(), m.moneyIn(), m.moneyOut())).toList(),
                s.topCategories().stream().map(c -> new CategoryItem(c.categoryId(), c.name(), c.amount())).toList());
    }
}