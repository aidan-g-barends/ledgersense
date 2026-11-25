package com.ledgersense.dto;

import com.ledgersense.insights.TimelinePoint;

import java.math.BigDecimal;
import java.time.LocalDate;

// NEW: matches TimelinePoint in the frontend's types.ts exactly
public record TimelinePointResponse(LocalDate date, BigDecimal moneyIn, BigDecimal moneyOut,
                                    BigDecimal runningNet, int flagCount) {

    public static TimelinePointResponse from(TimelinePoint p) {
        return new TimelinePointResponse(p.date(), p.moneyIn(), p.moneyOut(), p.runningNet(), p.flagCount());
    }
}