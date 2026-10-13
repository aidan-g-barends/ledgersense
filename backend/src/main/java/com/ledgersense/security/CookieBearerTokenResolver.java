package com.ledgersense.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

public class CookieBearerTokenResolver implements BearerTokenResolver {

    public static final String ACCESS_TOKEN_COOKIE = "access_token";


    @Override
    public String resolve(HttpServletRequest request) {
        // CHANGED: ignore the access cookie on ALL /api/auth/* endpoints except /me.
        // WHY: refresh and logout must work even when the access token has EXPIRED.
        // Otherwise the expired token gets a 401 before refresh can even run.
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/") && !path.equals("/api/auth/me")) {
            return null;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (AuthCookies.ACCESS.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
