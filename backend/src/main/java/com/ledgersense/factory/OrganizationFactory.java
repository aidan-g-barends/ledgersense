package com.ledgersense.factory;

import com.ledgersense.domain.Organization;
import com.ledgersense.util.Helper;

import java.time.Instant;
import java.util.Locale;

public class OrganizationFactory {

    public static Organization createOrganization( String name, String baseCurrency){

        if(Helper.isNullOrEmpty(name) || Helper.isNullOrEmpty(baseCurrency)){
            throw new IllegalArgumentException("Organization name cannot be null or empty! ");
        }

        if (!Helper.isWithinLength(name.strip(), 150)) {
            throw new IllegalArgumentException("Organization name must be at most 150 characters");
        }

        if (!Helper.isValidCurrencyCode(baseCurrency)) {
            throw new IllegalArgumentException("Base currency must be a valid 3-letter currency code");
        }

        return new Organization.Builder()
                .setName(name.strip())
                .setBaseCurrency(baseCurrency.strip().toUpperCase(Locale.ROOT))
                .build();
    }
}
