package com.ledgersense.util;

import java.math.BigDecimal;
import java.util.Locale;

public final class MoneyParser{

    private MoneyParser(){}

    public static BigDecimal parse(String raw){
        if(raw == null){
            return null;
        }

        // CHANGED: remove EVERY kind of space with one regex, not a typed " ".
        // \s = normal whitespace, \p{Zs} = all Unicode spaces (no-break, narrow no-break, thin...).
        // WHY: SA exports often use invisible "special" spaces as thousands separators.
        String s = raw.replaceAll("[\\s\\p{Zs}]", "")
                .replace("\u2212", "-")  // real minus sign \u2192 normal hyphen
                .toUpperCase(Locale.ROOT);

        if(s.isEmpty()){
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

        // WHY this block: "1,234.56" (English) vs "1 234,56" (South African). Rule:
        // if both appear, the LAST one is the decimal point. If only a comma appears,
        // 1 or 2 digits after it means decimal ("12,50"); 3 means thousands ("1,234").
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

        // Final gate: digits, optionally a dot and 1-2 decimals. Anything else = unreadable.
        // WHY max 2 decimals: a third decimal is a fraction of a cent, which means a parsing bug.
        if (!s.matches("\\d+(\\.\\d{1,2})?")) {
            return null;
        }
        BigDecimal value = new BigDecimal(s).setScale(2);
        return negative ? value.negate() : value;
    }
}

