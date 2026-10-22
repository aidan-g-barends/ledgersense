package com.ledgersense.dto;

import java.time.LocalDate;
import java.util.UUID;

// NEW: all the optional filters in one object. null = "don't filter on this".
public record TransactionFilter(UUID bankAccountId, UUID categoryId,
                                LocalDate from, LocalDate to, String q) {
}
