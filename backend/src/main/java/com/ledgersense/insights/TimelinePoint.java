package com.ledgersense.insights;

import java.math.BigDecimal;
import java.time.LocalDate;

// NEW: one day on the timeline chart
public record TimelinePoint(LocalDate date, BigDecimal moneyIn, BigDecimal moneyOut,
                            BigDecimal runningNet, int flagCount) {
}