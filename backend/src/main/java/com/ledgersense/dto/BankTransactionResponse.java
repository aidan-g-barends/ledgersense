package com.ledgersense.dto;

import com.ledgersense.domain.BankTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BankTransactionResponse(UUID id, LocalDate postedOn, String description,
                                      BigDecimal amount, BigDecimal balance,
                                      UUID bankAccountId, UUID categoryId,
                                      UUID merchantId) { // NEW

    public static BankTransactionResponse from(BankTransaction tx) {
        return new BankTransactionResponse(tx.getId(), tx.getPostedOn(), tx.getDescription(),
                tx.getAmount(), tx.getBalance(), tx.getBankAccountId(), tx.getCategoryId(),
                tx.getMerchantId()); // NEW
    }
}