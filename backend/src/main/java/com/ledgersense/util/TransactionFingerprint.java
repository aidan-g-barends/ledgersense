package com.ledgersense.util;

import com.ledgersense.security.TokenHasher;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

// NEW: builds the duplicate-detection key for one bank row.
public final class TransactionFingerprint {

    private TransactionFingerprint() {
    }

    public static String of(UUID bankAccountId, LocalDate postedOn, BigDecimal amount,
                            String description, int occurrence) {
        // WHY setScale(2): BigDecimal "10.0" and "10.00" are NOT equal as text.
        // Forcing 2 decimals means the same money always hashes the same.
        String money = amount.setScale(2, RoundingMode.UNNECESSARY).toPlainString();

        // WHY normalise: "WOOLWORTHS  CPT" and "woolworths cpt" are the same row
        String text = description.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);

        String key = bankAccountId + "|" + postedOn + "|" + money + "|" + text + "|" + occurrence;
        return TokenHasher.sha256Hex(key); // reusing the hasher from the refresh tokens
    }
}
