package com.ledgersense.insights;

import java.math.BigDecimal;
import java.time.YearMonth;

// NEW: one bar pair on the cash flow chart. Both amounts positive.
public record MonthTotal(YearMonth month, BigDecimal moneyIn, BigDecimal moneyOut) {
}