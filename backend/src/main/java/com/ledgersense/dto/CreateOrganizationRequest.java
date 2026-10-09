package com.ledgersense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// NEW: what the client is ALLOWED to send. Not the entity.
// WHY: with the entity as @RequestBody, a client could send "id" and overwrite things.
// record = immutable class, constructor + getters generated for you.
public record CreateOrganizationRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(min = 3, max = 3) String baseCurrency) {
}
