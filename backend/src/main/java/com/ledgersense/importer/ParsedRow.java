package com.ledgersense.importer;

import java.math.BigDecimal;
import java.time.LocalDate;

// One good row from the CSV, ready to become a BankTransaction
public record ParsedRow(int rowNumber, LocalDate postedOn, String description,
                        BigDecimal amount, BigDecimal balance) {
}