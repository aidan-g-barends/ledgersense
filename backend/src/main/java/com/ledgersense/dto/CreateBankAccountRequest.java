package com.ledgersense.dto;

import com.ledgersense.domain.Bank;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateBankAccountRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull Bank bank,
        @NotBlank @Pattern(regexp = "[0-9]{4}", message = "must be the last 4 digits only") String accountLast4,
        @Size(min = 3, max = 3) String currency
) {
    // NEW: compact constructor. Runs when the record is created.
    // WHY: currency is optional in the request, so if it's missing we default to ZAR.
    public CreateBankAccountRequest {
        if (currency == null || currency.isBlank()) {
            currency = "ZAR";
        }
    }
}