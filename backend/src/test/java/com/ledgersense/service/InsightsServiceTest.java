package com.ledgersense.service;

import com.ledgersense.domain.BankTransaction;
import com.ledgersense.insights.InsightsSummary;
import com.ledgersense.repository.BankTransactionRepository;
import com.ledgersense.service.impl.ICategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ledgersense.support.TestTransactions.tx;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsightsServiceTest {

    @Mock
    private BankTransactionRepository bankTransactionRepository;

    @Mock
    private ICategoryService categoryService;

    private InsightsService insightsService;
    private final UUID orgId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        insightsService = new InsightsService(bankTransactionRepository, categoryService);
    }

    @Test
    void summary_whenNoTransactions() {
        when(bankTransactionRepository.findFirstByOrgIdOrderByPostedOnDescCreatedAtDesc(orgId))
                .thenReturn(Optional.empty());

        assertEquals(0, insightsService.summary(orgId).transactionCount());
    }

    @Test
    void summary_totalsLatestMonth() {
        UUID food = UUID.randomUUID();
        BankTransaction september = tx(LocalDate.of(2026, 9, 10), "-100.00", null, food);
        BankTransaction income = tx(LocalDate.of(2026, 10, 1), "500.00", null, null);
        BankTransaction october = tx(LocalDate.of(2026, 10, 5), "-150.00", null, food);

        when(bankTransactionRepository.findFirstByOrgIdOrderByPostedOnDescCreatedAtDesc(orgId))
                .thenReturn(Optional.of(october));
        when(bankTransactionRepository.findAllByOrgIdAndPostedOnBetweenOrderByPostedOnAscCreatedAtAsc(
                orgId, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(september, income, october));

        InsightsSummary summary = insightsService.summary(orgId);

        // WHY compareTo: BigDecimal equals() says 150.0 != 150.00 (different scale). compareTo only checks the value.
        assertEquals(0, new BigDecimal("150").compareTo(summary.moneyOut()));
        assertEquals(0, new BigDecimal("350").compareTo(summary.net()));
        assertEquals(0, new BigDecimal("50").compareTo(summary.moneyOutChangePct())); // 100 -> 150 = +50%
        assertEquals(6, summary.months().size());
    }
}