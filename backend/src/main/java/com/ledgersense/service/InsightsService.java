package com.ledgersense.service;

import com.ledgersense.domain.BankTransaction;
import com.ledgersense.domain.Category;
import com.ledgersense.insights.CategorySpend;
import com.ledgersense.insights.InsightsSummary;
import com.ledgersense.insights.MonthTotal;
import com.ledgersense.repository.BankTransactionRepository;
import com.ledgersense.service.impl.ICategoryService;
import com.ledgersense.service.impl.IInsightsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.*;

@Service
public class InsightsService implements IInsightsService {

    static final int MONTHS = 6;
    static final int TOP_CATEGORIES = 6;

    private final BankTransactionRepository bankTransactionRepository;
    private final ICategoryService categoryService;

    public InsightsService(BankTransactionRepository bankTransactionRepository, ICategoryService categoryService) {
        this.bankTransactionRepository = bankTransactionRepository;
        this.categoryService = categoryService;
    }

    @Override
    @Transactional(readOnly = true)
    public InsightsSummary summary(UUID orgId) {
        Optional<BankTransaction> latest = bankTransactionRepository.findFirstByOrgIdOrderByPostedOnDescCreatedAtDesc(orgId);
        if (latest.isEmpty()) {
            return InsightsSummary.empty(YearMonth.now());
        }

        YearMonth month = YearMonth.from(latest.get().getPostedOn());
        YearMonth firstMonth = month.minusMonths(MONTHS - 1);
        List<BankTransaction> transactions = bankTransactionRepository
                .findAllByOrgIdAndPostedOnBetweenOrderByPostedOnAscCreatedAtAsc(
                        orgId, firstMonth.atDay(1), month.atEndOfMonth());

        // WHY pre-fill with zero: a quiet month still shows as an (empty) bar, not a missing one.
        // LinkedHashMap keeps the months in order, oldest first, like the chart draws them.
        Map<YearMonth, BigDecimal> moneyIn = new LinkedHashMap<>();
        Map<YearMonth, BigDecimal> moneyOut = new LinkedHashMap<>();
        for (int i = 0; i < MONTHS; i++) {
            moneyIn.put(firstMonth.plusMonths(i), BigDecimal.ZERO);
            moneyOut.put(firstMonth.plusMonths(i), BigDecimal.ZERO);
        }

        Map<UUID, BigDecimal> spendByCategory = new HashMap<>(); // HashMap allows a null key = uncategorised
        long count = 0;
        for (BankTransaction tx : transactions) {
            YearMonth txMonth = YearMonth.from(tx.getPostedOn());
            if (tx.isMoneyOut()) {
                moneyOut.merge(txMonth, tx.getAmount().abs(), BigDecimal::add); // merge = "add to what's there"
            } else {
                moneyIn.merge(txMonth, tx.getAmount(), BigDecimal::add);
            }
            if (txMonth.equals(month)) {
                count++;
                if (tx.isMoneyOut()) {
                    spendByCategory.merge(tx.getCategoryId(), tx.getAmount().abs(), BigDecimal::add);
                }
            }
        }

        List<MonthTotal> months = new ArrayList<>();
        for (YearMonth m : moneyIn.keySet()) {
            months.add(new MonthTotal(m, moneyIn.get(m), moneyOut.get(m)));
        }

        BigDecimal in = moneyIn.get(month);
        BigDecimal out = moneyOut.get(month);
        BigDecimal previousOut = moneyOut.get(month.minusMonths(1));
        // WHY null: "+∞% vs last month" is meaningless when last month was R0
        BigDecimal change = previousOut.signum() == 0
                ? null
                : out.subtract(previousOut).multiply(BigDecimal.valueOf(100)).divide(previousOut, 1, RoundingMode.HALF_UP);

        return new InsightsSummary(month, in, out, in.subtract(out), change, count, months,
                topCategories(orgId, spendByCategory));
    }

    private List<CategorySpend> topCategories(UUID orgId, Map<UUID, BigDecimal> spendByCategory) {
        Map<UUID, String> names = new HashMap<>();
        for (Category category : categoryService.readAll(orgId)) {
            names.put(category.getId(), category.getName());
        }
        return spendByCategory.entrySet().stream()
                .sorted(Map.Entry.<UUID, BigDecimal>comparingByValue().reversed()) // biggest spend first
                .limit(TOP_CATEGORIES)
                .map(e -> new CategorySpend(e.getKey(),
                        e.getKey() == null ? "Uncategorised" : names.getOrDefault(e.getKey(), "Unknown category"),
                        e.getValue()))
                .toList();
    }
}