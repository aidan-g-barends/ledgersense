package com.ledgersense.service.impl;

import com.ledgersense.insights.InsightsSummary;
import com.ledgersense.insights.TimelinePoint; // NEW

import java.util.List; // NEW
import java.util.UUID;

public interface IInsightsService {

    InsightsSummary summary(UUID orgId);

    // NEW: one point per day for the last `days` days, ending on the latest transaction. Oldest first.
    List<TimelinePoint> timeline(UUID orgId, int days);
}