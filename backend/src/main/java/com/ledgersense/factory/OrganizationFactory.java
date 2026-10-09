package com.ledgersense.factory;

import com.ledgersense.domain.Organization;
import com.ledgersense.util.Helper;

import java.time.Instant;
import java.util.Locale;

public class OrganizationFactory {

    public static Organization createOrganization( String name, String baseCurrency){

        if(Helper.isNullOrEmpty(name) || Helper.isNullOrEmpty(baseCurrency)){
           return null;
        }

        if (!Helper.isWithinLength(name.strip(), 150)) {
           return null;
        }

        if (!Helper.isValidCurrencyCode(baseCurrency)) {
            return null;
        }

        return new Organization.Builder()
                .setName(name.strip())
                .setBaseCurrency(baseCurrency.strip().toUpperCase(Locale.ROOT))
                .build();
    }
}
