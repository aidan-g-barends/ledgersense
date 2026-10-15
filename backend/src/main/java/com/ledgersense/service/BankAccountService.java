package com.ledgersense.service;

import com.ledgersense.domain.Bank;
import com.ledgersense.domain.BankAccount;
import com.ledgersense.exception.DuplicateResourceException;
import com.ledgersense.exception.ResourceNotFoundException;
import com.ledgersense.factory.BankAccountFactory;
import com.ledgersense.repository.IBankAccountRepository;
import com.ledgersense.service.impl.IBankAccountService;
import com.ledgersense.util.Helper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BankAccountService implements IBankAccountService {

    private final IBankAccountRepository bankAccountRepository;

    public BankAccountService(IBankAccountRepository bankAccountRepository){
        this.bankAccountRepository = bankAccountRepository;
    }
    @Override
    @Transactional
    public BankAccount create(UUID orgId, String name, Bank bank, String accountLast4, String currency) {
        BankAccount bankAccount = BankAccountFactory.create(orgId, name, bank, accountLast4, currency);
        if(bankAccount == null){
            throw new IllegalArgumentException("Invalid bank account details");
        }
        if (bankAccountRepository.existsByOrgIdAndBankAndAccountLast4(orgId, bank, accountLast4)) {
            throw new DuplicateResourceException("This bank account already exists");
        }
        return bankAccountRepository.save(bankAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccount> readAll(UUID orgId) {
        return bankAccountRepository.findAllByOrgId(orgId);
    }

    @Override
    @Transactional(readOnly = true)
    public BankAccount read(UUID id, UUID orgId) {
        return bankAccountRepository.findByIdAndOrgId(id,orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found"));

    }

    @Override
    @Transactional // without this, dirty checking never runs and the new name is never saved
    public BankAccount rename(UUID id, UUID orgId, String newName) {
        if (Helper.isNullOrBlank(newName) || !Helper.isWithinLength(newName.strip(), 100)) {
            throw new IllegalArgumentException("Invalid bank account name");
        }
        BankAccount account = read(id, orgId);
        account.rename(newName.strip()); // dirty checking saves it, no save() needed
        return account;
    }
}
