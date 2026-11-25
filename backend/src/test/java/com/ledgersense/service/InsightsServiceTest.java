package com.ledgersense.service;

import com.ledgersense.domain.BankTransaction;
import com.ledgersense.insights.InsightsSummary;
import com.ledgersense.insights.TimelinePoint; // NEW
import com.ledgersense.repository.BankTransactionRepository;
import com.ledgersense.repository.IAnomalyFlagRepository; // NEW
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
import static org.junit.jupiter.api.Assertions.assertThrows; // NEW
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsightsServiceTest {

    @Mock
    private BankTransactionRepository bankTransactionRepository;

    @Mock
    private ICategoryService categoryService;

    @Mock
    private IAnomalyFlagRepository anomalyFlagRepository; // NEW

    private InsightsService insightsService;
    private final UUID orgId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        insightsService = new InsightsService(bankTransactionRepository, categoryService, anomalyFlagRepository); // CHANGED
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

    // NEW: 7 days, money on only 2 of them. Every day still appears, and the running total adds up.
    @Test
    void timeline_fillsEveryDay() {
        BankTransaction income = tx(LocalDate.of(2026, 10, 1), "500.00", null, null);
        BankTransaction spend = tx(LocalDate.of(2026, 10, 5), "-150.00", null, null);
        LocalDate from = LocalDate.of(2026, 9, 29);
        LocalDate to = LocalDate.of(2026, 10, 5);

        when(bankTransactionRepository.findFirstByOrgIdOrderByPostedOnDescCreatedAtDesc(orgId))
                .thenReturn(Optional.of(spend));
        when(bankTransactionRepository.findAllByOrgIdAndPostedOnBetweenOrderByPostedOnAscCreatedAtAsc(orgId, from, to))
                .thenReturn(List.of(income, spend));
        when(anomalyFlagRepository.findFlaggedDays(orgId, from, to)).thenReturn(List.of(to));

        List<TimelinePoint> points = insightsService.timeline(orgId, 7);

        assertEquals(7, points.size());
        assertEquals(from, points.get(0).date());
        assertEquals(0, new BigDecimal("350").compareTo(points.get(6).runningNet()));
        assertEquals(1, points.get(6).flagCount());
    }

    @Test
    void timeline_rejectsBadRange() {
        assertThrows(IllegalArgumentException.class, () -> insightsService.timeline(orgId, 5));
    }
}