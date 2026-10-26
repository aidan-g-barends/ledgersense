package com.ledgersense.util;

import java.math.BigDecimal;
import java.util.Locale;

// Turns the many ways banks write money into an exact BigDecimal.
// Returns null if it can't be read (your factory style), never guesses.
public final class MoneyParser {

    private MoneyParser() {
    }

    public static BigDecimal parse(String raw) {
        if (raw == null) {
            return null;
        }
        // CHANGED: remove EVERY kind of space with one regex, not a typed " ".
        // \s = normal whitespace, \p{Zs} = all Unicode spaces (no-break, narrow no-break, thin...).
        String s = raw.replaceAll("[\\s\\p{Zs}]", "")
                .replace("\u2212", "-")  // real minus sign → normal hyphen
                .toUpperCase(Locale.ROOT);
        if (s.isEmpty()) {
            return null;
        }

        boolean negative = false;

        // Accounting style: (450.00) means minus 450
        if (s.startsWith("(") && s.endsWith(")")) {
            negative = true;
            s = s.substring(1, s.length() - 1);
        }
        // Some banks add DR (money out) or CR (money in) at the end
        if (s.endsWith("DR")) {
            negative = true;
            s = s.substring(0, s.length() - 2);
        } else if (s.endsWith("CR")) {
            s = s.substring(0, s.length() - 2);
        }

        // Drop the currency (AFTER the DR/CR check, because "DR" contains an R)
        s = s.replace("ZAR", "").replace("R", "");

        if (s.startsWith("-")) {
            negative = true;
            s = s.substring(1);
        } else if (s.startsWith("+")) {
            s = s.substring(1);
        }

        // "1,234.56" (English) vs "1 234,56" (South African). If both appear, the LAST one is
        // the decimal point. Only a comma: 1-2 digits after = decimal, 3 = thousands.
        int lastComma = s.lastIndexOf(',');
        int lastDot = s.lastIndexOf('.');
        if (lastComma >= 0 && lastDot >= 0) {
            s = lastComma > lastDot
                    ? s.replace(".", "").replace(',', '.')
                    : s.replace(",", "");
        } else if (lastComma >= 0) {
            int digitsAfter = s.length() - lastComma - 1;
            boolean onlyOneComma = s.indexOf(',') == lastComma;
            s = (onlyOneComma && digitsAfter <= 2) ? s.replace(',', '.') : s.replace(",", "");
        }

        // CHANGED: allow any number of decimals here, then check the real value below
        if (!s.matches("\\d+(\\.\\d+)?")) {
            return null;
        }
        // NEW: "7691.330000" is fine (extra digits are zeros), "12.345" is not (fraction of a cent).
        // stripTrailingZeros() turns 7691.330000 into 7691.33, then we check it fits in 2 decimals.
        BigDecimal value = new BigDecimal(s).stripTrailingZeros();
        if (value.scale() > 2) {
            return null;
        }
        value = value.setScale(2);
        return negative ? value.negate() : value;
    }
}