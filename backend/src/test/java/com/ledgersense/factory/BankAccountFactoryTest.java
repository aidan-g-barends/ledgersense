package com.ledgersense.factory;

import com.ledgersense.domain.Bank;
import com.ledgersense.domain.BankAccount;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BankAccountFactoryTest {

    private final UUID orgId = UUID.randomUUID();

    @Test
    void createBankAccount() {
        BankAccount account = BankAccountFactory.create(
                orgId, "  FNB Business Cheque  ", Bank.FNB, "4821", "zar");

        assertNotNull(account);
        assertEquals("FNB Business Cheque", account.getName());
        assertEquals("ZAR", account.getCurrency());
    }

    @Test
    void createBankAccount_withFullAccountNumber() {
        BankAccount account = BankAccountFactory.create(
                orgId, "FNB Business Cheque", Bank.FNB, "62012345678", "ZAR");

        assertNull(account);
    }

    @Test
    void createBankAccount_withoutOrg() {
        BankAccount account = BankAccountFactory.create(
                null, "FNB Business Cheque", Bank.FNB, "4821", "ZAR");

        assertNull(account);
    }
}