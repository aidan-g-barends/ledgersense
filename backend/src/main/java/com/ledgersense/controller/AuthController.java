package com.ledgersense.controller;

import com.ledgersense.domain.AppUser;
import com.ledgersense.dto.RegisterRequest;
import com.ledgersense.dto.RegisterResponse;
import com.ledgersense.service.AuthService;
import com.ledgersense.service.impl.IAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private IAuthService authService;

    public AuthController(IAuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request){
        AppUser user = authService.register(request.businessName(), request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(RegisterResponse.from(user));

    }
}
