package com.ledgersense.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateBankAccountRequest {

    @NotNull @Size(max=100) String name,
    @NotNull Bank bank,
    @NotBlank @Pattern(regexp = "[0-9]{4}", message = "must be the last 4 digits only") String accountLast4,
}
