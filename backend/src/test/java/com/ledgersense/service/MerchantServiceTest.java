package com.ledgersense.service;

import com.ledgersense.domain.Merchant;
import com.ledgersense.factory.MerchantFactory;
import com.ledgersense.repository.IMerchantRepository;
import com.ledgersense.service.impl.IMerchantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {

    @Mock
    private IMerchantRepository merchantRepository;

    private IMerchantService merchantService;
    private final UUID orgId = UUID.randomUUID();
    private final LocalDate date = LocalDate.of(2026, 10, 1);

    @BeforeEach
    void setUp() {
        merchantService = new MerchantService(merchantRepository);
    }

    @Test
    void findOrCreate_reusesExistingAndCreatesMissing() {
        Merchant existing = MerchantFactory.createMerchant(orgId, "SPAR LANGEBAAN", date);
        Map<String, LocalDate> names = new HashMap<>();
        names.put("SPAR LANGEBAAN", date);
        names.put("ENGEN GARAGE", date);

        when(merchantRepository.findAllByOrgIdAndNameIn(orgId, names.keySet())).thenReturn(List.of(existing));
        when(merchantRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Merchant> result = merchantService.findOrCreate(orgId, names);

        assertEquals(2, result.size());
        assertSame(existing, result.get("SPAR LANGEBAAN")); // reused, not duplicated
    }
}