package com.ledgersense.importer;

import java.util.List;
import java.util.Locale;

// NEW: turns a messy bank description into a stable merchant name.
// Same shop on different days must give the SAME name, or grouping breaks.
public final class MerchantNameNormalizer {

    // Payment processors that sit in FRONT of the real shop name
    private static final List<String> PREFIXES = List.of(
            "POS PURCHASE ", "CARD PURCHASE ", "DEBIT ORDER ",
            "YOCO *", "IK *", "SQ *", "ZAPPER *", "SNAPSCAN *", "PAYFAST *");

    private MerchantNameNormalizer() {
    }

    // Returns null when nothing meaningful is left (your factory style)
    public static String normalize(String description) {
        if (description == null) {
            return null;
        }
        String s = description.toUpperCase(Locale.ROOT);
        s = s.replaceAll("\\*{4}\\d{4}", " ");                                // masked cards: ****6311
        s = s.replaceAll("\\d{1,2}/\\d{1,2}( ?- ?\\d{1,2}/\\d{1,2})?", " ");  // fee periods: 27/08 - 25/09
        s = s.strip();
        for (String prefix : PREFIXES) {
            if (s.startsWith(prefix)) {
                s = s.substring(prefix.length());
                break;
            }
        }
        s = s.replaceAll("\\d{4,}", " ");                // long digit runs: refs, terminal ids
        s = s.replaceAll("[^A-Z0-9&.'/ -]", " ");        // stray symbols like *
        s = s.replaceAll("\\s+", " ").strip();
        s = s.replaceAll("^[-./ ]+|[-./ ]+$", "");       // leftover punctuation at the ends
        if (s.length() < 2) {
            return null;
        }
        return s.length() > 100 ? s.substring(0, 100).strip() : s;
    }
}