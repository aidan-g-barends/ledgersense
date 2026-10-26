package com.ledgersense.importer;

import com.ledgersense.domain.Bank;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CsvStatementParserTest {

    private final CsvStatementParser parser = new CsvStatementParser();

    @Test
    void parse_singleAmountColumn() throws IOException {
        String csv = """
                Date,Description,Amount,Balance
                2026/10/01,POS PURCHASE WOOLWORTHS CPT,-450.50,12549.50
                2026/10/02,"SALARY, ACME TRADING",15000.00,27549.50
                """;
        ColumnMapping mapping = new ColumnMapping(',', 0, true, "uuuu/MM/dd",
                0, 1, 2, null, null, 3);

        ParseResult result = parser.parse(new StringReader(csv), mapping);

        assertEquals(2, result.rows().size());
        assertEquals(0, result.errors().size());
        assertEquals(new BigDecimal("-450.50"), result.rows().get(0).amount());
        assertEquals("SALARY, ACME TRADING", result.rows().get(1).description()); // comma inside quotes survived
    }

    @Test
    void parse_debitCreditColumnsWithJunkLinesOnTop() throws IOException {
        String csv = """
                Account: Business Cheque ****1234
                Statement period: October 2026
                Date;Description;Debit;Credit;Balance
                01/10/2026;ENGEN GARAGE;800,00;;11749,50
                02/10/2026;EFT FROM CLIENT;;2 500,00;14249,50
                """;
        ColumnMapping mapping = new ColumnMapping(';', 2, true, "dd/MM/uuuu",
                0, 1, null, 2, 3, 4);

        ParseResult result = parser.parse(new StringReader(csv), mapping);

        assertEquals(2, result.rows().size());
        assertEquals(new BigDecimal("-800.00"), result.rows().get(0).amount());
        assertEquals(new BigDecimal("2500.00"), result.rows().get(1).amount());
        assertEquals(LocalDate.of(2026, 10, 1), result.rows().get(0).postedOn());
    }

    @Test
    void parse_badRowIsReportedNotFatal() throws IOException {
        String csv = """
                Date,Description,Amount
                2026/10/01,WOOLWORTHS,-450.50
                2026/02/30,IMPOSSIBLE DATE,-10.00
                2026/10/03,SPAR,-99.99
                """;
        ColumnMapping mapping = new ColumnMapping(',', 0, true, "uuuu/MM/dd",
                0, 1, 2, null, null, null);

        ParseResult result = parser.parse(new StringReader(csv), mapping);

        assertEquals(2, result.rows().size());   // the good rows still came through
        assertEquals(1, result.errors().size()); // the bad one was reported
        assertEquals(3, result.errors().get(0).rowNumber());
    }

    // NEW: reads the committed fixture file from src/test/resources/fixtures.
    // getResourceAsStream works the same on your PC and in GitHub Actions (no C:\ paths).
    @Test
    void parse_absaFixtureFile() throws IOException {
        InputStream stream = getClass().getResourceAsStream("/fixtures/absa-statement-sample.csv");
        ParseResult result = parser.parse(
                new InputStreamReader(stream, StandardCharsets.UTF_8), BankCsvPresets.forBank(Bank.ABSA));

        assertEquals(12, result.rows().size());
        assertEquals(0, result.errors().size());
        assertEquals(LocalDate.of(2026, 9, 26), result.rows().get(0).postedOn());          // "Sept" handled
        assertEquals("Fresh Mart Seaside ****1234", result.rows().get(1).description());  // card masked
        assertEquals(new BigDecimal("10154.40"), result.rows().get(1).balance());          // padded balance
        assertEquals("MUNICIPAL FINE - 1234567890", result.rows().get(7).description());  // short ref kept
    }
}