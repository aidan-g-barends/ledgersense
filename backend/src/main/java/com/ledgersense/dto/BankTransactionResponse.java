package com.ledgersense.dto;

import com.ledgersense.domain.BankTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

// WHY no fingerprint or orgId: internal details the frontend never needs
public record BankTransactionResponse(UUID id, LocalDate postedOn, String description,
                                      BigDecimal amount, BigDecimal balance,
                                      UUID bankAccountId, UUID categoryId) {

    public static BankTransactionResponse from(BankTransaction tx) {
        return new BankTransactionResponse(tx.getId(), tx.getPostedOn(), tx.getDescription(),
                tx.getAmount(), tx.getBalance(), tx.getBankAccountId(), tx.getCategoryId());
    }
}