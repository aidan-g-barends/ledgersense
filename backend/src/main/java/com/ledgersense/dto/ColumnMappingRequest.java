package com.ledgersense.dto;

import com.ledgersense.importer.ColumnMapping;
import jakarta.validation.constraints.*;

// CHANGED: every field is optional now. Nothing sent = use the bank's preset.
public record ColumnMappingRequest(
        @Pattern(regexp = ",|;|TAB", message = "must be , or ; or TAB") String delimiter,
        @Min(0) @Max(50) Integer skipLines,
        Boolean hasHeader,
        @Pattern(regexp = "[dMuy/\\-. ]{6,20}", message = "must be a date pattern like dd/MM/yyyy")
        String datePattern,
        @Min(0) @Max(100) Integer dateColumn,
        @Min(0) @Max(100) Integer descriptionColumn,
        @Min(0) @Max(100) Integer amountColumn,
        @Min(0) @Max(100) Integer debitColumn,
        @Min(0) @Max(100) Integer creditColumn,
        @Min(0) @Max(100) Integer balanceColumn
) {
    // NEW: did the client send a mapping at all?
    public boolean isProvided() {
        return delimiter != null;
    }

    public ColumnMapping toMapping() {
        // NEW: a half-filled mapping is the client's mistake = 400, not a NullPointerException 500
        if (datePattern == null || dateColumn == null || descriptionColumn == null) {
            throw new IllegalArgumentException("Column mapping is incomplete: date pattern, date column and description column are required");
        }
        char separator = "TAB".equals(delimiter) ? '\t' : delimiter.charAt(0);
        return new ColumnMapping(
                separator,
                skipLines == null ? 0 : skipLines,
                hasHeader == null || hasHeader,
                datePattern.replace('y', 'u'),   // users type yyyy, STRICT parsing needs uuuu
                dateColumn,
                descriptionColumn,
                amountColumn,
                debitColumn,
                creditColumn,
                balanceColumn);
    }
}