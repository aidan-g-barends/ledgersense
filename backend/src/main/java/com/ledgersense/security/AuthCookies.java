package com.ledgersense.security;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

// NEW: all cookie settings in ONE place, so login, refresh and logout can't drift apart
public final class AuthCookies {

    public static final String ACCESS = "access_token";
    public static final String REFRESH = "refresh_token";

    private AuthCookies() {
    }

    public static ResponseCookie access(String token, Duration ttl) {
        return build(ACCESS, token, "/", ttl, "Lax");
    }

    // path "/api/auth": the browser ONLY sends it to refresh/logout, never to normal endpoints.
    // Strict: never sent from another site at all.
    public static ResponseCookie refresh(String rawToken, Duration ttl) {
        return build(REFRESH, rawToken, "/api/auth", ttl, "Strict");
    }

    // maxAge 0 = "delete this cookie now"
    public static ResponseCookie clearAccess() {
        return build(ACCESS, "", "/", Duration.ZERO, "Lax");
    }

    public static ResponseCookie clearRefresh() {
        return build(REFRESH, "", "/api/auth", Duration.ZERO, "Strict");
    }

    private static ResponseCookie build(String name, String value, String path, Duration maxAge, String sameSite) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite(sameSite)
                .path(path)
                .maxAge(maxAge)
                .build();
    }
}