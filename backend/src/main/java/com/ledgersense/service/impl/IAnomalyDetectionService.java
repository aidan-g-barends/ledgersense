package com.ledgersense.service.impl;

import com.ledgersense.domain.BankTransaction;

import java.util.List;
import java.util.UUID;

public interface IAnomalyDetectionService {

    // NEW: runs every DetectionRule over freshly imported rows. Returns how many flags were raised.
    int detect(UUID orgId, List<BankTransaction> newTransactions);
}