package com.ledgersense.importer;

import java.util.List;

// Everything the parser found: the good rows AND the problems, side by side
public record ParseResult(List<ParsedRow> rows, List<RowError> errors) {
}