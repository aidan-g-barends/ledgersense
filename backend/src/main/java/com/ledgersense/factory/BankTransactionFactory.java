package com.ledgersense.factory;

import com.ledgersense.domain.BankTransaction;
import com.ledgersense.util.Helper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class BankTransactionFactory {

    public static BankTransaction createBankTransaction(UUID orgId, UUID bankAccountId, UUID importBatchId,
                                                        LocalDate postedOn, String description,
                                                        BigDecimal amount, BigDecimal balance,
                                                        String fingerprint) {
        if (orgId == null || bankAccountId == null || importBatchId == null) {
            return null;
        }
        // WHY reject future dates: a bank can't have posted something that hasn't happened yet.
        // Usually means the date column was parsed wrong (e.g. MM/dd vs dd/MM).
        if (postedOn == null || postedOn.isAfter(LocalDate.now())) {
            return null;
        }
        if (Helper.isNullOrBlank(description)) {
            return null;
        }
        String cleanDescription = description.strip().replaceAll("\\s+", " ");
        if (!Helper.isWithinLength(cleanDescription, 500)) {
            return null;
        }
        // WHY: more than 2 real decimals = fractions of a cent. That's a parsing bug, not money.
        if (amount == null || amount.stripTrailingZeros().scale() > 2) {
            return null;
        }
        if (balance != null && balance.stripTrailingZeros().scale() > 2) {
            return null;
        }
        if (fingerprint == null || fingerprint.length() != 64) {
            return null;
        }

        return new BankTransaction.Builder()
                .setOrgId(orgId)
                .setBankAccountId(bankAccountId)
                .setImportBatchId(importBatchId)
                .setPostedOn(postedOn)
                .setDescription(cleanDescription)
                .setAmount(amount.setScale(2))   // safe: we already know it fits in 2 decimals
                .setBalance(balance == null ? null : balance.setScale(2))
                .setFingerprint(fingerprint)
                .build();
    }
}
