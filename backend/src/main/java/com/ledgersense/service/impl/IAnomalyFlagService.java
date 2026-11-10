package com.ledgersense.service.impl;

import com.ledgersense.domain.AnomalyFlag;
import com.ledgersense.domain.BankTransaction;
import com.ledgersense.domain.FlagStatus;

import java.util.List;
import java.util.UUID;

public interface IAnomalyFlagService {

    // NEW: a flag + its transaction, because the frontend contract embeds the transaction
    record FlagView(AnomalyFlag flag, BankTransaction transaction) {
    }

    List<FlagView> list(UUID orgId, FlagStatus status);

    FlagView review(UUID id, UUID orgId, UUID userId, FlagStatus decision);

    // Same merchant, newest first, max 12, includes the flagged one
    List<BankTransaction> history(UUID id, UUID orgId);
}