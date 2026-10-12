package com.ledgersense.service;

import com.ledgersense.domain.AppUser;
import com.ledgersense.domain.Organization;
import com.ledgersense.domain.Role;
import com.ledgersense.service.impl.IAppUserService;
import com.ledgersense.service.impl.IAuthService;
import com.ledgersense.service.impl.IOrganizationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Service
public class AuthService implements IAuthService {

    private static final int MIN_PASSWORD_LENGTH=12;
    private static final int MAX_PASSWORD_BYTES=72;

    private  IOrganizationService organizationService;
    private  IAppUserService appUserService;
    private  PasswordEncoder passwordEncoder;


    public AuthService(IOrganizationService organizationService, IAppUserService appUserService, PasswordEncoder passwordEncoder){

        this.organizationService = organizationService;
        this.appUserService = appUserService;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    @Transactional
    public AppUser register(String businessName, String email, String password) {

        validatePassword(password);

        Organization organization = organizationService.create(businessName, "ZAR");
        String passwordHash = passwordEncoder.encode(password);
        return appUserService.create(organization.getId(), email, passwordHash, Role.OWNER);
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
