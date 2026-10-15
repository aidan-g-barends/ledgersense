package com.ledgersense.util;

import java.util.Currency;
import java.util.Locale;

public class Helper {

    public static boolean isNullOrEmpty(String string){
        return string == null || string.isEmpty();
    }

    // NEW: like isNullOrEmpty, but "   " (only spaces/tabs) also counts as blank
    public static boolean isNullOrBlank(String string){
        return string == null || string.isBlank();
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

    }

    public static boolean isValidEmail(String email){
        if(isNullOrEmpty(email)){
            return false;
        }
        String e = email.strip();
        int at = e.indexOf('@');
        return at > 0
                && at == e.lastIndexOf('@')
                && e.indexOf('.', at) > at + 1
                && !e.endsWith(".");
    }
    // NEW: exactly 4 digits, nothing else. "1234" ok, "12345678" or "12a4" not.
    public static boolean isLast4Digits(String value) {
        return value != null && value.matches("[0-9]{4}");
    }

}
