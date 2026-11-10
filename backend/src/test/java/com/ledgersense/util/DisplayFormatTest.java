package com.ledgersense.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DisplayFormatTest {

    @Test
    void money() {
        assertEquals("R 2 850,50", DisplayFormat.money(new BigDecimal("-2850.50")));
    }

    @Test
    void date() {
        assertEquals("5 Oct 2026", DisplayFormat.date(LocalDate.of(2026, 10, 5)));
    }
}