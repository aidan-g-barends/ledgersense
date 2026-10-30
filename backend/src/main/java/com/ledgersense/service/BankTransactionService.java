package com.ledgersense.service;

import com.ledgersense.domain.BankTransaction;
import com.ledgersense.domain.Merchant;
import com.ledgersense.dto.TransactionFilter;
import com.ledgersense.exception.ResourceNotFoundException;
import com.ledgersense.repository.BankTransactionRepository;
import com.ledgersense.repository.BankTransactionSpecs;
import com.ledgersense.service.impl.IBankTransactionService;
import com.ledgersense.service.impl.ICategoryService;
import com.ledgersense.service.impl.IMerchantService;
import com.ledgersense.util.Helper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BankTransactionService implements IBankTransactionService {

    static final int MAX_PAGE_SIZE = 100;
    static final int MAX_SEARCH_LENGTH = 100;

    private final BankTransactionRepository bankTransactionRepository;
    private final ICategoryService categoryService;   // NEW
    private final IMerchantService merchantService;   // NEW

    public BankTransactionService(BankTransactionRepository bankTransactionRepository,
                                  ICategoryService categoryService,
                                  IMerchantService merchantService) {
        this.bankTransactionRepository = bankTransactionRepository;
        this.categoryService = categoryService;
        this.merchantService = merchantService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BankTransaction> page(UUID orgId, TransactionFilter filter, int page, int size) {
        if (filter.from() != null && filter.to() != null && filter.from().isAfter(filter.to())) {
            throw new IllegalArgumentException("'from' must be on or before 'to'");
        }
        if (filter.q() != null && filter.q().length() > MAX_SEARCH_LENGTH) {
            throw new IllegalArgumentException("Search text is too long");
        }

        List<Specification<BankTransaction>> specs = new ArrayList<>();
        specs.add(BankTransactionSpecs.inOrg(orgId));
        if (filter.bankAccountId() != null) {
            specs.add(BankTransactionSpecs.inAccount(filter.bankAccountId()));
        }
        if (filter.categoryId() != null) {
            specs.add(BankTransactionSpecs.inCategory(filter.categoryId()));
        }
        if (filter.from() != null) {
            specs.add(BankTransactionSpecs.postedOnOrAfter(filter.from()));
        }
        if (filter.to() != null) {
            specs.add(BankTransactionSpecs.postedOnOrBefore(filter.to()));
        }
        if (!Helper.isNullOrBlank(filter.q())) {
            specs.add(BankTransactionSpecs.descriptionContains(filter.q()));
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize,
                Sort.by(Sort.Order.desc("postedOn"), Sort.Order.desc("createdAt")));

        return bankTransactionRepository.findAll(Specification.allOf(specs), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public BankTransaction read(UUID id, UUID orgId) {
        return bankTransactionRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }

    // NEW
    @Override
    @Transactional
    public RecategoriseResult recategorise(UUID id, UUID orgId, UUID categoryId, boolean rememberMerchant) {
        BankTransaction tx = read(id, orgId); // 404 if not this org's

        // SECURITY: categoryId came from the client. 404 if it's another org's category.
        if (categoryId != null) {
            categoryService.read(categoryId, orgId);
        }
        tx.categorise(categoryId);

        int merchantUpdated = 0;
        if (rememberMerchant && tx.getMerchantId() != null) {
            // Teach the merchant (future imports) AND fix every existing row (bulk UPDATE)
            Merchant merchant = merchantService.read(tx.getMerchantId(), orgId);
            merchant.assignCategory(categoryId);
            merchantUpdated = bankTransactionRepository.recategoriseMerchant(
                    orgId, tx.getMerchantId(), categoryId, Instant.now());
        }
        return new RecategoriseResult(tx, merchantUpdated);
    }
}