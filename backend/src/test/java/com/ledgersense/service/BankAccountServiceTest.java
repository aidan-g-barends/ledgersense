package com.ledgersense.service;

import com.ledgersense.domain.Bank;
import com.ledgersense.domain.BankAccount;
import com.ledgersense.exception.DuplicateResourceException;
import com.ledgersense.exception.ResourceNotFoundException;
import com.ledgersense.factory.BankAccountFactory;
import com.ledgersense.repository.IBankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

    @Mock
    private IBankAccountRepository bankAccountRepository;

    private BankAccountService bankAccountService;
    private final UUID orgId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        bankAccountService = new BankAccountService(bankAccountRepository);
    }

    @Test
    void create() {
        BankAccount account = BankAccountFactory.create(orgId, "FNB Cheque", Bank.FNB, "4821", "ZAR");
        when(bankAccountRepository.existsByOrgIdAndBankAndAccountLast4(orgId, Bank.FNB, "4821")).thenReturn(false);
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(account);

        BankAccount created = bankAccountService.create(orgId, "FNB Cheque", Bank.FNB, "4821", "ZAR");

        assertEquals("FNB Cheque", created.getName());
    }

    @Test
    void create_whenDuplicate() {
        when(bankAccountRepository.existsByOrgIdAndBankAndAccountLast4(orgId, Bank.FNB, "4821")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> bankAccountService.create(orgId, "FNB Cheque", Bank.FNB, "4821", "ZAR"));
    }

    @Test
    void read_whenOtherOrg() {
        UUID id = UUID.randomUUID();
        when(bankAccountRepository.findByIdAndOrgId(id, orgId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bankAccountService.read(id, orgId));
    }
}