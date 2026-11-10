package com.ledgersense.dto;

import com.ledgersense.domain.FlagStatus;
import jakarta.validation.constraints.NotNull;

// NEW: { "status": "LEGITIMATE" } or { "status": "DISPUTED" }. "OPEN" is rejected by the entity.
public record ReviewFlagRequest(@NotNull FlagStatus status) {
}
