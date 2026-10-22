package com.ledgersense.security;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// NEW: gets the caller's IP from the current HTTP request, from anywhere in the code.
// Returns null when there's no request (e.g. in a unit test or a background job).
public final class ClientIp {

    private ClientIp() {
    }

    public static String current() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest().getRemoteAddr();
        }
        return null;
    }
}