package com.ledgersense.service;

import com.ledgersense.domain.AppUser;
import com.ledgersense.domain.Organization;
import com.ledgersense.domain.Role;
import com.ledgersense.exception.InvalidCredentialsException;
import com.ledgersense.exception.ResourceNotFoundException;
import com.ledgersense.service.impl.IAppUserService;
import com.ledgersense.service.impl.IAuthService;
import com.ledgersense.service.impl.ICategoryService;
import com.ledgersense.service.impl.IOrganizationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class AuthService implements IAuthService {

    private static final int MIN_PASSWORD_LENGTH=12;
    private static final int MAX_PASSWORD_BYTES=72;

    private  IOrganizationService organizationService;
    private  IAppUserService appUserService;
    private  PasswordEncoder passwordEncoder;
    private final ICategoryService categoryService; // NEW

    // NEW: a fake hash used when the email doesn't exist (see login below)
    private final String dummyHash;


    public AuthService(IOrganizationService organizationService, IAppUserService appUserService, PasswordEncoder passwordEncoder,
                       ICategoryService categoryService){

        this.organizationService = organizationService;
        this.appUserService = appUserService;
        this.passwordEncoder = passwordEncoder;
        this.categoryService = categoryService;
        this.dummyHash = passwordEncoder.encode("timing-attack-dummy-password");
    }
    @Override
    @Transactional
    public AppUser register(String businessName, String email, String password) {

        validatePassword(password);

        Organization organization = organizationService.create(businessName, "ZAR");

        // NEW: give the new org its starter categories.
        // WHY here: register is @Transactional, so if seeding fails, the org + user roll back too.
        categoryService.seedDefaults(organization.getId());

        String passwordHash = passwordEncoder.encode(password);
        return appUserService.create(organization.getId(), email, passwordHash, Role.OWNER);
    }

    @Override
    @Transactional
    public AppUser login(String email, String password) {
        AppUser user;
        try {
            user = appUserService.findByEmail(email);
        } catch (ResourceNotFoundException e) {
            // TIMING ATTACK FIX: checking a BCrypt hash takes ~100ms.
            // If unknown emails returned instantly, attackers could time responses
            // to find out which emails have accounts. So we do the same slow work either way.
            passwordEncoder.matches(password, dummyHash);
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        // No save() needed: the user is managed inside this transaction (dirty checking)
        user.recordLogin(Instant.now());
        return user;
    }

    private void validatePassword(String password){
        if(password == null || password.length() < MIN_PASSWORD_LENGTH){
            throw new IllegalArgumentException("Password must be atleast 12 characters");
        }

        if(password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES){
            throw new IllegalArgumentException("Password is too long");
        }
    }
}
