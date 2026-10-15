package com.ledgersense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameBankAccountRequest(@NotBlank @Size(max = 100) String name) {
}