package com.ledgersense.util;

import java.util.Currency;
import java.util.Locale;

public class Helper {

    public static boolean isNullOrEmpty(String string){
        return string == null || string.isEmpty();
    }

    public static boolean isWithinLength(String value, int max) {
        return value != null && value.length() <= max;
    }

    // NEW: uses Java's built-in list of real ISO currencies.
    // Currency.getInstance("RAND") throws, so we return false.
    public static boolean isValidCurrencyCode(String code) {
        if (isNullOrEmpty(code)) {
            return false;
        }
        try {
            Currency.getInstance(code.strip().toUpperCase(Locale.ROOT));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }

    }}
