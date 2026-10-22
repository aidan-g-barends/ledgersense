package com.ledgersense.service;

import com.ledgersense.dto.TransactionFilter;
import com.ledgersense.exception.ResourceNotFoundException;
import com.ledgersense.repository.BankTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankTransactionServiceTest {

    @Mock
    private BankTransactionRepository bankTransactionRepository;

    private BankTransactionService bankTransactionService;
    private final UUID orgId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        bankTransactionService = new BankTransactionService(bankTransactionRepository);
    }

    @Test
    void page_whenFromAfterTo() {
        TransactionFilter filter = new TransactionFilter(null, null,
                LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 1), null);

        assertThrows(IllegalArgumentException.class,
                () -> bankTransactionService.page(orgId, filter, 0, 50));
    }

    @Test
    void read_whenOtherOrg() {
        UUID id = UUID.randomUUID();
        when(bankTransactionRepository.findByIdAndOrgId(id, orgId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bankTransactionService.read(id, orgId));
    }
}
