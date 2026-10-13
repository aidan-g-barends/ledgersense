package com.ledgersense.controller;

import com.ledgersense.domain.AppUser;
import com.ledgersense.domain.Organization;
import com.ledgersense.dto.LoginRequest;
import com.ledgersense.dto.MeResponse;
import com.ledgersense.dto.RegisterRequest;
import com.ledgersense.dto.RegisterResponse;
import com.ledgersense.security.AuthCookies;
import com.ledgersense.security.JwtService;
import com.ledgersense.service.impl.IAppUserService;
import com.ledgersense.service.impl.IAuthService;
import com.ledgersense.service.impl.IOrganizationService;
import com.ledgersense.service.impl.IRefreshTokenService;
import com.ledgersense.service.impl.IRefreshTokenService.RotationResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService authService;
    private final JwtService jwtService;
    private final IRefreshTokenService refreshTokenService;
    private final IAppUserService appUserService;
    private final IOrganizationService organizationService;

    public AuthController(IAuthService authService, JwtService jwtService,
                          IRefreshTokenService refreshTokenService,
                          IAppUserService appUserService, IOrganizationService organizationService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.appUserService = appUserService;
        this.organizationService = organizationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        AppUser user = authService.register(request.businessName(), request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(RegisterResponse.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<MeResponse> login(@Valid @RequestBody LoginRequest request) {
        AppUser user = authService.login(request.email(), request.password());
        // CHANGED: login now also issues a refresh token
        String refreshToken = refreshTokenService.issue(user.getId());
        return withAuthCookies(user, refreshToken);
    }

    // NEW: swap an old refresh token for a fresh pair. @CookieValue reads the cookie directly.
    @PostMapping("/refresh")
    public ResponseEntity<MeResponse> refresh(
            @CookieValue(name = AuthCookies.REFRESH, required = false) String refreshToken) {
        RotationResult result = refreshTokenService.rotate(refreshToken);
        AppUser user = appUserService.read(result.userId());
        return withAuthCookies(user, result.newRawToken());
    }

    // NEW: revoke in the DB + tell the browser to delete both cookies. 204 = success, no body.
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = AuthCookies.REFRESH, required = false) String refreshToken) {
        refreshTokenService.revoke(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, AuthCookies.clearAccess().toString())
                .header(HttpHeaders.SET_COOKIE, AuthCookies.clearRefresh().toString())
                .build();
    }

    // NEW: the frontend calls this once on page load. It makes Spring set the XSRF-TOKEN cookie.
    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        AppUser user = appUserService.read(UUID.fromString(jwt.getSubject()));
        Organization org = organizationService.read(user.getOrgId());
        return MeResponse.from(user, org);
    }

    // NEW: login and refresh both send the same two cookies + the user's details
    private ResponseEntity<MeResponse> withAuthCookies(AppUser user, String refreshToken) {
        String accessToken = jwtService.issueAccessToken(user);
        Organization org = organizationService.read(user.getOrgId());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, AuthCookies.access(accessToken, jwtService.getAccessTokenTtl()).toString())
                .header(HttpHeaders.SET_COOKIE, AuthCookies.refresh(refreshToken, refreshTokenService.getTtl()).toString())
                .body(MeResponse.from(user, org));
    }
}