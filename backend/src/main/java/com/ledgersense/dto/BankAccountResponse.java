package com.ledgersense.dto;

import com.ledgersense.domain.Bank;
import com.ledgersense.domain.BankAccount;
import com.ledgersense.importer.BankCsvPresets;

import java.time.Instant;
import java.util.UUID;

public record BankAccountResponse(UUID id, String name, Bank bank, String accountLast4,
                                  String currency, Instant createdAt,
                                  boolean hasCsvPreset) { // NEW: lets the wizard skip column mapping

    public static BankAccountResponse from(BankAccount account) {
        return new BankAccountResponse(
                account.getId(),
                account.getName(),
                account.getBank(),
                account.getAccountLast4(),
                account.getCurrency(),
                account.getCreatedAt(),
                BankCsvPresets.forBank(account.getBank()) != null); // NEW
    }
}