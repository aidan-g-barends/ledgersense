package com.ledgersense.importer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// NEW: SECURITY. Banks glue card numbers onto descriptions ("Fresh Mart4000...").
// We never want a full card number in our database, logs or API responses.
public final class DescriptionSanitizer {

    // 12 to 19 digits in a row = card-number length. Shorter numbers (references, fines) are left alone.
    private static final Pattern CARD_NUMBER = Pattern.compile("\\d{12,19}");

    private DescriptionSanitizer() {
    }

    public static String clean(String description) {
        Matcher matcher = CARD_NUMBER.matcher(description);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String digits = matcher.group();
            // Keep only the last 4, same policy as bank account numbers
            matcher.appendReplacement(result, " ****" + digits.substring(digits.length() - 4));
        }
        matcher.appendTail(result);
        // Tidy up: the inserted space may double up with an existing one
        return result.toString().replaceAll("\\s+", " ").strip();
    }
}