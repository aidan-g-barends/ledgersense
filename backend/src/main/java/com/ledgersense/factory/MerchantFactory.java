package com.ledgersense.factory;

import com.ledgersense.domain.Merchant;
import com.ledgersense.util.Helper;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

public class MerchantFactory {

    public static Merchant createMerchant(UUID orgId, String name, LocalDate firstSeenOn) {
        if (orgId == null || firstSeenOn == null || Helper.isNullOrBlank(name)) {
            return null;
        }
        String cleanName = name.strip().toUpperCase(Locale.ROOT);
        if (cleanName.length() < 2 || !Helper.isWithinLength(cleanName, 100)) {
            return null;
        }
        return new Merchant.Builder()
                .setOrgId(orgId)
                .setName(cleanName)
                .setFirstSeenOn(firstSeenOn)
                .build();
    }
}