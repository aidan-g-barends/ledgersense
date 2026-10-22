package com.ledgersense.service.impl;

import com.ledgersense.domain.BankTransaction;
import com.ledgersense.dto.TransactionFilter;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface IBankTransactionService {

    Page<BankTransaction> page(UUID orgId, TransactionFilter filter, int page, int size);

    BankTransaction read(UUID id, UUID orgId);
}