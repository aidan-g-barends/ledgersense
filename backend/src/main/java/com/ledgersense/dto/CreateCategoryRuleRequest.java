package com.ledgersense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCategoryRuleRequest(
        @NotNull UUID categoryId,
        @NotBlank @Size(min = 2, max = 100) String keyword
) {
}