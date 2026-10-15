package com.ledgersense.service.impl;

import com.ledgersense.domain.Bank;
import com.ledgersense.domain.BankAccount;

import java.util.List;
import java.util.UUID;

public interface IBankAccountService {

    BankAccount create(UUID orgId, String name, Bank bank, String accountLast4, String currency);

    List<BankAccount> readAll(UUID orgId);

    BankAccount read(UUID id, UUID orgId);

    BankAccount rename(UUID id, UUID orgId, String name);
}
