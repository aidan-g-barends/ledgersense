package com.ledgersense.importer;

import com.ledgersense.util.MoneyParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// NEW: turns CSV text into ParsedRows using a ColumnMapping.
// SECURITY: never log row contents. It's someone's private banking data.
@Component
public class CsvStatementParser {

    static final int MAX_ROWS = 5000;

    public ParseResult parse(Reader input, ColumnMapping mapping) throws IOException {
        List<ParsedRow> rows = new ArrayList<>();
        List<RowError> errors = new ArrayList<>();

        BufferedReader reader = new BufferedReader(input);
        skipByteOrderMark(reader);
        for (int i = 0; i < mapping.skipLines(); i++) {
            if (reader.readLine() == null) {
                break;
            }
        }

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(mapping.delimiter())
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .get();

        DateTimeFormatter dates = DateTimeFormatter.ofPattern(mapping.datePattern(), Locale.ROOT)
                .withResolverStyle(ResolverStyle.STRICT); // rejects impossible dates like 30 Feb

        // try-with-resources: the parser (and file) is closed even if something throws
        try (CSVParser parser = format.parse(reader)) {
            boolean firstRecord = true;
            for (CSVRecord record : parser) {
                int rowNumber = mapping.skipLines() + (int) record.getRecordNumber();

                if (firstRecord && mapping.hasHeader()) {
                    firstRecord = false;
                    continue; // skip the header row
                }
                firstRecord = false;

                if (rows.size() + errors.size() >= MAX_ROWS) {
                    errors.add(new RowError(rowNumber,
                            "File has more than " + MAX_ROWS + " rows. Split it and import the rest separately."));
                    break;
                }
                parseRow(record, rowNumber, mapping, dates, rows, errors);
            }
        }
        return new ParseResult(rows, errors);
    }

    private void parseRow(CSVRecord record, int rowNumber, ColumnMapping mapping, DateTimeFormatter dates,
                          List<ParsedRow> rows, List<RowError> errors) {
        int needed = mapping.maxColumnIndex() + 1;
        if (record.size() < needed) {
            errors.add(new RowError(rowNumber, "Expected " + needed + " columns but found " + record.size()));
            return;
        }

        String rawDate = record.get(mapping.dateColumn());
        LocalDate postedOn;
        try {
            postedOn = LocalDate.parse(rawDate, dates);
        } catch (DateTimeParseException ex) {
            errors.add(new RowError(rowNumber, "Couldn't read the date '" + preview(rawDate) + "'"));
            return;
        }

        String description = record.get(mapping.descriptionColumn());
        if (description.isBlank()) {
            errors.add(new RowError(rowNumber, "Description is empty"));
            return;
        }

        BigDecimal amount = readAmount(record, mapping);
        if (amount == null) {
            errors.add(new RowError(rowNumber, "Couldn't read the amount"));
            return;
        }

        BigDecimal balance = mapping.balanceColumn() == null
                ? null
                : MoneyParser.parse(record.get(mapping.balanceColumn())); // balance is optional, so null is fine

        rows.add(new ParsedRow(rowNumber, postedOn, description.strip(), amount, balance));
    }

    // WHY two styles: some banks give one signed Amount column, others give
    // separate Debit (money out) and Credit (money in) columns. We turn both into one signed number.
    private BigDecimal readAmount(CSVRecord record, ColumnMapping mapping) {
        if (mapping.amountColumn() != null) {
            return MoneyParser.parse(record.get(mapping.amountColumn()));
        }
        BigDecimal debit = MoneyParser.parse(record.get(mapping.debitColumn()));
        BigDecimal credit = MoneyParser.parse(record.get(mapping.creditColumn()));
        if (debit == null && credit == null) {
            return null;
        }
        BigDecimal moneyIn = credit == null ? BigDecimal.ZERO : credit.abs();
        BigDecimal moneyOut = debit == null ? BigDecimal.ZERO : debit.abs();
        return moneyIn.subtract(moneyOut).setScale(2);
    }

    // WHY: Excel on Windows often saves CSVs with an invisible "BOM" character at the very start.
    // Left in, it would glue itself onto the first header and break column matching.
    private void skipByteOrderMark(BufferedReader reader) throws IOException {
        reader.mark(1);
        if (reader.read() != '\uFEFF') {
            reader.reset();
        }
    }

    // Keep error messages short: never echo a giant cell back
    private String preview(String value) {
        return value.length() > 30 ? value.substring(0, 30) + "…" : value;
    }
}