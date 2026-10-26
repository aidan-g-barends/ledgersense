package com.ledgersense.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyParserTest {

    @Test
    void parse_plainNegative() {
        assertEquals(new BigDecimal("-450.50"), MoneyParser.parse("-450.50"));
    }

    @Test
    void parse_southAfricanFormat() {
        assertEquals(new BigDecimal("1234.56"), MoneyParser.parse("R 1 234,56"));
    }

    @Test
    void parse_englishThousands() {
        assertEquals(new BigDecimal("1234.56"), MoneyParser.parse("1,234.56"));
    }

    @Test
    void parse_bracketsAndDebit() {
        assertEquals(new BigDecimal("-450.00"), MoneyParser.parse("(450.00)"));
        assertEquals(new BigDecimal("-450.00"), MoneyParser.parse("450.00 DR"));
    }

    // NEW: some banks pad balances with zeros
    @Test
    void parse_trailingZeroDecimals() {
        assertEquals(new BigDecimal("7691.33"), MoneyParser.parse("7691.330000"));
    }

    @Test
    void parse_unreadable() {
        assertNull(MoneyParser.parse("abc"));
        assertNull(MoneyParser.parse("12.345")); // fraction of a cent
        assertNull(MoneyParser.parse("   "));
    }
}