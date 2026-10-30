package com.ledgersense.service;

import com.ledgersense.domain.BankTransaction;
import com.ledgersense.dto.TransactionFilter;
import com.ledgersense.exception.ResourceNotFoundException;
import com.ledgersense.factory.BankTransactionFactory;
import com.ledgersense.repository.BankTransactionRepository;
import com.ledgersense.service.impl.ICategoryService;
import com.ledgersense.service.impl.IMerchantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankTransactionServiceTest {

    @Mock
    private BankTransactionRepository bankTransactionRepository;

    @Mock
    private ICategoryService categoryService; // NEW

    @Mock
    private IMerchantService merchantService; // NEW

    private BankTransactionService bankTransactionService;
    private final UUID orgId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        bankTransactionService = new BankTransactionService(
                bankTransactionRepository, categoryService, merchantService); // CHANGED
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

    // NEW: the chosen category belongs to another org, so nothing changes
    @Test
    void recategorise_withOtherOrgsCategory() {
        UUID txId = UUID.randomUUID();
        UUID otherOrgsCategory = UUID.randomUUID();
        BankTransaction tx = BankTransactionFactory.createBankTransaction(orgId, UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().minusDays(1), "SPAR", new BigDecimal("-10.00"), null, "a".repeat(64));

        when(bankTransactionRepository.findByIdAndOrgId(txId, orgId)).thenReturn(Optional.of(tx));
        when(categoryService.read(otherOrgsCategory, orgId))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> bankTransactionService.recategorise(txId, orgId, otherOrgsCategory, false));
    }
}