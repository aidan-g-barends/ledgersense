package com.ledgersense.service.impl;

import com.ledgersense.domain.BankTransaction;
import com.ledgersense.dto.TransactionFilter;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface IBankTransactionService {

    // NEW: the changed transaction + how many merchant rows were updated (0 if not remembered)
    record RecategoriseResult(BankTransaction transaction, int merchantTransactionsUpdated) {
    }

    Page<BankTransaction> page(UUID orgId, TransactionFilter filter, int page, int size);

    BankTransaction read(UUID id, UUID orgId);

    // categoryId null = uncategorise
    RecategoriseResult recategorise(UUID id, UUID orgId, UUID categoryId, boolean rememberMerchant);
}