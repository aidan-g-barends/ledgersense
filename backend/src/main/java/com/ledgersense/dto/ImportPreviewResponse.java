package com.ledgersense.dto;

import com.ledgersense.importer.ParseResult;
import com.ledgersense.importer.ParsedRow;
import com.ledgersense.importer.RowError;

import java.util.List;

// NEW: what the preview screen shows. Counts for the whole file, but only a sample of rows.
public record ImportPreviewResponse(int totalRows, int validRows, int errorCount,
                                    List<ParsedRow> rows, List<RowError> errors) {

    static final int SAMPLE_ROWS = 50;
    static final int MAX_ERRORS = 100;

    // WHY a sample: 5 000 rows of JSON for a preview is pointless. Show the first 50 and the totals.
    public static ImportPreviewResponse from(ParseResult result) {
        return new ImportPreviewResponse(
                result.rows().size() + result.errors().size(),
                result.rows().size(),
                result.errors().size(),
                result.rows().stream().limit(SAMPLE_ROWS).toList(),
                result.errors().stream().limit(MAX_ERRORS).toList());
    }
}