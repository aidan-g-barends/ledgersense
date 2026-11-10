package com.ledgersense.service;

import com.ledgersense.domain.AnomalyFlag;
import com.ledgersense.domain.BankTransaction;
import com.ledgersense.domain.FlagStatus;
import com.ledgersense.exception.ResourceNotFoundException;
import com.ledgersense.repository.BankTransactionRepository;
import com.ledgersense.repository.IAnomalyFlagRepository;
import com.ledgersense.service.impl.IAnomalyFlagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class AnomalyFlagService implements IAnomalyFlagService {

    private final IAnomalyFlagRepository anomalyFlagRepository;
    private final BankTransactionRepository bankTransactionRepository;

    public AnomalyFlagService(IAnomalyFlagRepository anomalyFlagRepository,
                              BankTransactionRepository bankTransactionRepository) {
        this.anomalyFlagRepository = anomalyFlagRepository;
        this.bankTransactionRepository = bankTransactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlagView> list(UUID orgId, FlagStatus status) {
        List<AnomalyFlag> flags = anomalyFlagRepository.findTop200ByOrgIdAndStatusOrderByCreatedAtDesc(orgId, status);

        // ONE query for every flagged transaction (not one per flag = N+1)
        List<UUID> ids = flags.stream().map(AnomalyFlag::getTransactionId).toList();
        Map<UUID, BankTransaction> byId = new HashMap<>();
        for (BankTransaction tx : bankTransactionRepository.findAllById(ids)) {
            byId.put(tx.getId(), tx);
        }

        List<FlagView> views = new ArrayList<>();
        for (AnomalyFlag flag : flags) {
            BankTransaction tx = byId.get(flag.getTransactionId());
            if (tx != null) {
                views.add(new FlagView(flag, tx));
            }
        }
        return views;
    }

    @Override
    @Transactional
    public FlagView review(UUID id, UUID orgId, UUID userId, FlagStatus decision) {
        AnomalyFlag flag = findFlag(id, orgId);       // 404 if it's another org's flag
        flag.review(decision, userId, Instant.now()); // 400 if decision is OPEN (the entity guards it)
        return new FlagView(flag, transactionOf(flag, orgId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankTransaction> history(UUID id, UUID orgId) {
        BankTransaction tx = transactionOf(findFlag(id, orgId), orgId);
        if (tx.getMerchantId() != null) {
            return bankTransactionRepository.findTop12ByOrgIdAndMerchantIdOrderByPostedOnDescCreatedAtDesc(
                    orgId, tx.getMerchantId());
        }
        return bankTransactionRepository.findTop12ByOrgIdAndDescriptionOrderByPostedOnDescCreatedAtDesc(
                orgId, tx.getDescription());
    }

    private AnomalyFlag findFlag(UUID id, UUID orgId) {
        return anomalyFlagRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Flag not found"));
    }

    private BankTransaction transactionOf(AnomalyFlag flag, UUID orgId) {
        return bankTransactionRepository.findByIdAndOrgId(flag.getTransactionId(), orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }
}