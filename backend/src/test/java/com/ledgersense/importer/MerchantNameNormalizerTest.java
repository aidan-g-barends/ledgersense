package com.ledgersense.importer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MerchantNameNormalizerTest {

    @Test
    void normalize_removesCardAndProcessor() {
        assertEquals("FRESH MART SEASIDE", MerchantNameNormalizer.normalize("Fresh Mart Seaside ****1234"));
        assertEquals("CROSSO", MerchantNameNormalizer.normalize("Yoco *Crosso ****1234"));
    }

    @Test
    void normalize_removesReferencesAndPeriods() {
        assertEquals("MUNICIPAL FINE", MerchantNameNormalizer.normalize("MUNICIPAL FINE - 1234567890"));
        assertEquals("SERVICE FEE", MerchantNameNormalizer.normalize("SERVICE FEE 27/08 - 25/09"));
    }

    @Test
    void normalize_nothingLeft() {
        assertNull(MerchantNameNormalizer.normalize("1234567890"));
    }
}