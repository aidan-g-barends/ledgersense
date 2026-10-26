package com.ledgersense.importer;

import java.util.List;
import java.util.UUID;

// NEW: what the user sees after an import: "12 imported, 3 duplicates skipped, 1 error"
public record ImportResult(UUID batchId, String fileName, int totalRows, int imported,
                           int duplicates, int errorCount, List<RowError> errors) {
}