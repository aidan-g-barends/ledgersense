package com.ledgersense.importer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DescriptionSanitizerTest {

    @Test
    void clean_masksGluedAndSpacedCardNumbers() {
        assertEquals("Fresh Mart ****1234", DescriptionSanitizer.clean("Fresh Mart4000123412341234"));
        assertEquals("Fuel Stop ****1234", DescriptionSanitizer.clean("Fuel Stop 4000123412341234"));
    }

    @Test
    void clean_leavesShortReferencesAlone() {
        assertEquals("MUNICIPAL FINE - 1234567890", DescriptionSanitizer.clean("MUNICIPAL FINE - 1234567890"));
    }
}