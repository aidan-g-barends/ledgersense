package com.ledgersense.factory;

import com.ledgersense.domain.Bank;
import com.ledgersense.domain.BankAccount;
import com.ledgersense.util.Helper;

import java.util.Locale;
import java.util.UUID;

public class BankAccountFactory {

    public static BankAccount create(UUID orgId, String name, Bank bank, String accountLast4, String currency){

        if(orgId == null || bank == null){
            return null;
        }

        // CHANGED: isNullOrBlank (rejects "   "), and ! on isWithinLength (reject if TOO LONG, not if it fits)
        if(Helper.isNullOrBlank(name) || !Helper.isWithinLength(name.strip(), 100)){
            return null;
        }

        // CHANGED: ! added. Reject when it is NOT exactly 4 digits.
        if(!Helper.isLast4Digits(accountLast4)){
            return null;
        }

        // CHANGED: normalise first, THEN validate. "zar" becomes "ZAR" before the check.
        if (Helper.isNullOrBlank(currency)) {
            return null;
        }
        String currencyCode = currency.strip().toUpperCase(Locale.ROOT);
        if (!Helper.isValidCurrencyCode(currencyCode)) {
            return null;
        }

        return new BankAccount.Builder()
                .setOrgId(orgId)
                .setName(name.strip())
                .setBank(bank)
                .setAccountLast4(accountLast4)
                .setCurrency(currencyCode) // CHANGED: use the already-normalised code
                .build();

    }
}
