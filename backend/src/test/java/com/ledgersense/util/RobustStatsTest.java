package com.ledgersense.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RobustStatsTest {

    @Test
    void median() {
        assertEquals(2.0, RobustStats.median(new double[]{3, 1, 2}));
        assertEquals(2.5, RobustStats.median(new double[]{1, 2, 3, 4}));
    }

    @Test
    void mad() {
        // median 2, distances 1,1,0,0,2,4,7, median of those = 1
        assertEquals(1.0, RobustStats.mad(new double[]{1, 1, 2, 2, 4, 6, 9}));
    }
}