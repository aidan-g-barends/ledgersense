package com.ledgersense.importer;

import com.ledgersense.domain.Bank;

// NEW: known CSV layouts per bank, so users of these banks never map columns by hand.
// Returns null for banks without a preset yet (your factory style). The wizard then asks the user.
public final class BankCsvPresets {

    private BankCsvPresets() {
    }

    public static ColumnMapping forBank(Bank bank) {
        if (bank == null) {
            return null;
        }
        // Switch expression: each case returns a value directly, no break needed
        return switch (bank) {
            // Layout: 4 info lines, NO header row, then date,description,amount,balance
            // e.g. 26Sept2026,Fresh Mart Seaside,-345.6,10154.400000
            case ABSA -> new ColumnMapping(',', 4, false, "ddMMMuuuu",
                    0, 1, 2, null, null, 3);
            default -> null;
        };
    }
}