package com.ledgersense.detection;

import com.ledgersense.domain.AnomalyFlag;
import com.ledgersense.domain.BankTransaction;
import com.ledgersense.domain.FlagRule;

import java.util.List;
import java.util.UUID;

// NEW: STRATEGY PATTERN. Every detection rule is one class that implements this.
// Spring collects them all into a List<DetectionRule>, so a new rule = one new @Component, nothing else changes.
public interface DetectionRule {

    FlagRule rule();

    /**
     * @param newTransactions just imported (already saved, so they have ids). Only THESE can be flagged.
     * @param window          the org's recent transactions INCLUDING the new ones, oldest first.
     *                        Used for context: "has this merchant charged before?"
     */
    List<AnomalyFlag> detect(UUID orgId, List<BankTransaction> newTransactions, List<BankTransaction> window);
}