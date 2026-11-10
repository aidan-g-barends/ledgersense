package com.ledgersense.service;

import com.ledgersense.detection.DetectionRule;
import com.ledgersense.domain.AnomalyFlag;
import com.ledgersense.domain.BankTransaction;
import com.ledgersense.repository.BankTransactionRepository;
import com.ledgersense.repository.IAnomalyFlagRepository;
import com.ledgersense.service.impl.IAnomalyDetectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AnomalyDetectionService implements IAnomalyDetectionService {

    // How far back rules look for context ("did this merchant charge last month?")
    static final int HISTORY_DAYS = 120;

    private final List<DetectionRule> rules; // WHY a List: Spring injects EVERY DetectionRule bean it finds
    private final BankTransactionRepository bankTransactionRepository;
    private final IAnomalyFlagRepository anomalyFlagRepository;

    public AnomalyDetectionService(List<DetectionRule> rules,
                                   BankTransactionRepository bankTransactionRepository,
                                   IAnomalyFlagRepository anomalyFlagRepository) {
        this.rules = rules;
        this.bankTransactionRepository = bankTransactionRepository;
        this.anomalyFlagRepository = anomalyFlagRepository;
    }

    @Override
    @Transactional
    public int detect(UUID orgId, List<BankTransaction> newTransactions) {
        if (newTransactions == null || newTransactions.isEmpty()) {
            return 0;
        }

        LocalDate earliest = newTransactions.get(0).getPostedOn();
        LocalDate latest = earliest;
        for (BankTransaction tx : newTransactions) {
            if (tx.getPostedOn().isBefore(earliest)) earliest = tx.getPostedOn();
            if (tx.getPostedOn().isAfter(latest)) latest = tx.getPostedOn();
        }

        // ONE query for all the context every rule needs
        List<BankTransaction> window = bankTransactionRepository
                .findAllByOrgIdAndPostedOnBetweenOrderByPostedOnAscCreatedAtAsc(
                        orgId, earliest.minusDays(HISTORY_DAYS), latest);

        List<AnomalyFlag> flags = new ArrayList<>();
        for (DetectionRule rule : rules) {
            flags.addAll(rule.detect(orgId, newTransactions, window));
        }
        anomalyFlagRepository.saveAll(flags);
        return flags.size();
    }
}