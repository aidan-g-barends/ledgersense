package com.ledgersense.dto;

import com.ledgersense.domain.Bank;
import com.ledgersense.domain.BankAccount;

import java.time.Instant;
import java.util.UUID;

// WHY no orgId in the response: the client already knows its own org. Don't send what isn't needed.
public record BankAccountResponse(UUID id, String name, Bank bank, String accountLast4, String currency, Instant createdAt) {

    public static BankAccountResponse from(BankAccount bankAccount) {
        return new BankAccountResponse(
                bankAccount.getId(),
                bankAccount.getName(),
                bankAccount.getBank(),
                bankAccount.getAccountLast4(),
                bankAccount.getCurrency(),
                bankAccount.getCreatedAt());
    }
}