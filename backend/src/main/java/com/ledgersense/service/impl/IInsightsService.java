package com.ledgersense.service.impl;

import com.ledgersense.insights.InsightsSummary;

import java.util.UUID;

public interface IInsightsService {

    // NEW: totals for the latest month with data, plus the 6-month trend and top categories
    InsightsSummary summary(UUID orgId);
}