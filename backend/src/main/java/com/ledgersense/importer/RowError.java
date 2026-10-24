package com.ledgersense.importer;

// One bad row: where it is and what's wrong, in plain English for the preview screen
public record RowError(int rowNumber, String message) {
}