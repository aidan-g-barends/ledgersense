package com.ledgersense.service;

import com.ledgersense.domain.AnomalyFlag;
import com.ledgersense.domain.FlagEvidence;
import com.ledgersense.domain.FlagRule;
import com.ledgersense.domain.FlagSeverity;
import com.ledgersense.domain.FlagStatus;
import com.ledgersense.exception.ResourceNotFoundException;
import com.ledgersense.factory.AnomalyFlagFactory;
import com.ledgersense.repository.BankTransactionRepository;
import com.ledgersense.repository.IAnomalyFlagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnomalyFlagServiceTest {

    @Mock
    private IAnomalyFlagRepository anomalyFlagRepository;

    @Mock
    private BankTransactionRepository bankTransactionRepository;

    private AnomalyFlagService anomalyFlagService;
    private final UUID orgId = UUID.randomUUID();
    private final UUID flagId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        anomalyFlagService = new AnomalyFlagService(anomalyFlagRepository, bankTransactionRepository);
    }

    @Test
    void review_whenOtherOrg() {
        when(anomalyFlagRepository.findByIdAndOrgId(flagId, orgId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> anomalyFlagService.review(flagId, orgId, UUID.randomUUID(), FlagStatus.LEGITIMATE));
    }

    @Test
    void review_backToOpen() {
        AnomalyFlag flag = AnomalyFlagFactory.createAnomalyFlag(orgId, UUID.randomUUID(), FlagRule.DUPLICATE_CHARGE,
                FlagSeverity.WARNING, "Possible duplicate charge", "Charged twice.",
                List.of(new FlagEvidence("Each charge", "R 299,00")));
        when(anomalyFlagRepository.findByIdAndOrgId(flagId, orgId)).thenReturn(Optional.of(flag));

        assertThrows(IllegalArgumentException.class,
                () -> anomalyFlagService.review(flagId, orgId, UUID.randomUUID(), FlagStatus.OPEN));
    }
}