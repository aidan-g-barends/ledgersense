package com.ledgersense.dto;

import java.util.UUID;

// categoryId null = uncategorise. rememberMerchant missing = false.
public record RecategoriseRequest(UUID categoryId, boolean rememberMerchant) {
}