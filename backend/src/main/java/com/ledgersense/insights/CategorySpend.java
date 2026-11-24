package com.ledgersense.insights;

import java.math.BigDecimal;
import java.util.UUID;

// NEW: categoryId null = uncategorised spending
public record CategorySpend(UUID categoryId, String name, BigDecimal amount) {
}