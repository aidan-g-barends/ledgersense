package com.ledgersense.factory;

import com.ledgersense.domain.Merchant;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MerchantFactoryTest {

    @Test
    void createMerchant() {
        Merchant merchant = MerchantFactory.createMerchant(UUID.randomUUID(), " spar langebaan ", LocalDate.of(2026, 9, 26));

        assertNotNull(merchant);
        assertEquals("SPAR LANGEBAAN", merchant.getName());
    }

    @Test
    void createMerchant_withoutDate() {
        Merchant merchant = MerchantFactory.createMerchant(UUID.randomUUID(), "SPAR", null);

        assertNull(merchant);
    }
}