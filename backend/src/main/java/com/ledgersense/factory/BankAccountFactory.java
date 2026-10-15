package com.ledgersense.factory;

import com.ledgersense.domain.Bank;
import com.ledgersense.domain.BankAccount;
import com.ledgersense.util.Helper;

import java.util.UUID;

public class BankAccountFactory {

    public static BankAccount create(UUID orgId, String name, Bank bank, String accountLast4, String currency){

        if(orgId == null || bank == null){
            return null;
        }

        if(Helper.isNullOrEmpty(name) || Helper.isWithinLength(name.strip() , 100)){
            return null;
        }

        if(Helper.isLast4Digits(accountLast4)){
            return null;
        }

        return new BankAccount.Builder()
                .setOrgId(orgId)
                .setName(name)
                .setBank(bank)
                .setAccountLast4(accountLast4)
                .setCurrency(currency)
                .build();

    }
}
