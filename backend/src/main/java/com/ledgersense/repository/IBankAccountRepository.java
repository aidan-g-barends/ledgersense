package com.ledgersense.repository;

import com.ledgersense.domain.Bank;
import com.ledgersense.domain.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IBankAccountRepository extends JpaRepository <BankAccount, UUID> {

    List<BankAccount> findAllByOrgId(UUID orgId);

    Optional<BankAccount> findByIdAndOrgId(UUID id, UUID orgId);

    boolean existsByOrgIdAndBankAndAccountLast4(UUID id, Bank bank, String accountLast4);
}
