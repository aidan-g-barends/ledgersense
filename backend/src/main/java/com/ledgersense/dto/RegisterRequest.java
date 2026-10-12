package com.ledgersense.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// What the client SENDS. @Valid in the controller checks these rules
// and returns a 400 automatically if any fail.
public record RegisterRequest(
        @NotBlank @Size(max = 150) String businessName,
        @NotBlank @Email @Size(max = 254) String email,
        // 12 minimum (length beats complexity rules).
        // 72 maximum: BCrypt silently ignores anything longer.
        @NotBlank @Size(min = 12, max = 72) String password) {
}