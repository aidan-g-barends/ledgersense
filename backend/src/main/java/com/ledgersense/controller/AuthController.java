package com.ledgersense.controller;

import com.ledgersense.domain.AppUser;
import com.ledgersense.domain.Organization;
import com.ledgersense.dto.LoginRequest;
import com.ledgersense.dto.MeResponse;
import com.ledgersense.dto.RegisterRequest;
import com.ledgersense.dto.RegisterResponse;
import com.ledgersense.security.CookieBearerTokenResolver;
import com.ledgersense.security.JwtService;
import com.ledgersense.service.impl.IAppUserService;
import com.ledgersense.service.impl.IAuthService;
import com.ledgersense.service.impl.IOrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService authService;
    private final JwtService jwtService;
    private final IAppUserService appUserService;
    private final IOrganizationService organizationService;

    public AuthController(IAuthService authService, JwtService jwtService,
                          IAppUserService appUserService, IOrganizationService organizationService) {
        this.authService = authService;
        this.jwtService = jwtService;
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
        String token = jwtService.issueAccessToken(user);

        // NEW: the token goes in a cookie, NOT the JSON body
        ResponseCookie cookie = ResponseCookie.from(CookieBearerTokenResolver.ACCESS_TOKEN_COOKIE, token)
                .httpOnly(true)      // JavaScript can't read it (XSS protection)
                .secure(true)        // HTTPS only (browsers allow localhost as an exception)
                .sameSite("Lax")     // not sent on cross-site POSTs (CSRF protection)
                .path("/")
                .maxAge(jwtService.getAccessTokenTtl())
                .build();

        Organization org = organizationService.read(user.getOrgId());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(MeResponse.from(user, org));
    }

    // NEW: only reachable with a valid token.
    // @AuthenticationPrincipal hands us the checked JWT. "subject" = the user id we put in.
    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        AppUser user = appUserService.read(UUID.fromString(jwt.getSubject()));
        Organization org = organizationService.read(user.getOrgId());
        return MeResponse.from(user, org);
    }
}