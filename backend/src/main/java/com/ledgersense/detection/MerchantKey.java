package com.ledgersense.detection;

import com.ledgersense.domain.BankTransaction;

import java.util.Locale;

// NEW: "who was paid", shared by every rule. Package-private: only detection rules need it.
final class MerchantKey {

    private MerchantKey() {
    }

    // WHY prefer merchantId: "CLOUD HOST ****5678" and "CLOUD HOST ****9999" are the same merchant
    static String of(BankTransaction tx) {
        return tx.getMerchantId() != null
                ? tx.getMerchantId().toString()
                : tx.getDescription().toUpperCase(Locale.ROOT);
    }
}