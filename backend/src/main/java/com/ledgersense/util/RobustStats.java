package com.ledgersense.util;

import java.util.Arrays;

// NEW: statistics that one extreme value can't drag around (unlike average and standard deviation)
public final class RobustStats {

    private RobustStats() {
    }

    // The middle value. Average of [100, 110, 5000] = 1737 (useless). Median = 110 (honest).
    public static double median(double[] values) {
        if (values.length == 0) {
            throw new IllegalArgumentException("No values");
        }
        double[] sorted = values.clone(); // WHY clone: never reorder the caller's array behind their back
        Arrays.sort(sorted);
        int mid = sorted.length / 2;
        return sorted.length % 2 == 1 ? sorted[mid] : (sorted[mid - 1] + sorted[mid]) / 2.0;
    }

    // Median Absolute Deviation: the median of "how far is each value from the median"
    public static double mad(double[] values) {
        double median = median(values);
        double[] deviations = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            deviations[i] = Math.abs(values[i] - median);
        }
        return median(deviations);
    }

    // WHY 0.6745: scales MAD so the score reads like a normal z-score (in "standard deviations")
    public static double robustZ(double value, double median, double mad) {
        return 0.6745 * (value - median) / mad;
    }
}