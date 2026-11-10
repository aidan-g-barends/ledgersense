package com.ledgersense.dto;

import com.ledgersense.domain.AnomalyFlag;
import com.ledgersense.domain.FlagRule;
import com.ledgersense.domain.FlagSeverity;
import com.ledgersense.domain.FlagStatus;
import com.ledgersense.service.impl.IAnomalyFlagService.FlagView;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// NEW: field names match AnomalyFlag in the frontend's types.ts EXACTLY. That's the contract.
public record AnomalyFlagResponse(UUID id, FlagRule rule, FlagSeverity severity, FlagStatus status,
                                  String title, String explanation, List<Evidence> evidence,
                                  BankTransactionResponse transaction,
                                  Instant createdAt, Instant reviewedAt) {

    public record Evidence(String label, String value) {
    }

    public static AnomalyFlagResponse from(FlagView view) {
        AnomalyFlag flag = view.flag();
        List<Evidence> evidence = flag.getEvidence().stream()
                .map(item -> new Evidence(item.getLabel(), item.getValue()))
                .toList();
        return new AnomalyFlagResponse(flag.getId(), flag.getRule(), flag.getSeverity(), flag.getStatus(),
                flag.getTitle(), flag.getExplanation(), evidence,
                BankTransactionResponse.from(view.transaction()),
                flag.getCreatedAt(), flag.getReviewedAt());
    }
}