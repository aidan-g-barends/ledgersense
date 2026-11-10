package com.ledgersense.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// NEW: how money and dates are WRITTEN in flag evidence ("R 2 850,00", "5 Oct 2026")
public final class DisplayFormat {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH);

    private DisplayFormat() {
    }

    // -2850.5 -> "R 2 850,50". SA style: space between thousands, comma before cents.
    public static String money(BigDecimal amount) {
        // WHY Locale.ROOT first: gives a predictable "2,850.50" on every machine, then we swap the symbols
        String plain = String.format(Locale.ROOT, "%,.2f", amount.abs());
        return "R " + plain.replace(',', ' ').replace('.', ',');
    }

    public static String date(LocalDate date) {
        return DATE.format(date);
    }
}