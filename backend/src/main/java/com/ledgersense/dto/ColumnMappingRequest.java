package com.ledgersense.dto;

import com.ledgersense.importer.ColumnMapping;
import jakarta.validation.constraints.*;

// NEW: the mapping as it arrives from the upload form (form fields, not JSON).
// Spring fills a record straight from form fields with the same names.
public record ColumnMappingRequest(
        @NotNull @Pattern(regexp = ",|;|TAB", message = "must be , or ; or TAB") String delimiter,
        @Min(0) @Max(50) Integer skipLines,
        Boolean hasHeader,
        // WHY a strict pattern: only day/month/year letters and separators.
        // Stops weird input reaching DateTimeFormatter.
        @NotBlank @Pattern(regexp = "[dMuy/\\-. ]{6,20}", message = "must be a date pattern like dd/MM/yyyy")
        String datePattern,
        @NotNull @Min(0) @Max(100) Integer dateColumn,
        @NotNull @Min(0) @Max(100) Integer descriptionColumn,
        @Min(0) @Max(100) Integer amountColumn,
        @Min(0) @Max(100) Integer debitColumn,
        @Min(0) @Max(100) Integer creditColumn,
        @Min(0) @Max(100) Integer balanceColumn
) {
    public ColumnMapping toMapping() {
        char separator = "TAB".equals(delimiter) ? '\t' : delimiter.charAt(0);
        return new ColumnMapping(
                separator,
                skipLines == null ? 0 : skipLines,
                hasHeader == null || hasHeader,           // default: yes, there's a header
                datePattern.replace('y', 'u'),            // WHY: users type yyyy, STRICT parsing needs uuuu
                dateColumn,
                descriptionColumn,
                amountColumn,
                debitColumn,
                creditColumn,
                balanceColumn);
    }
}