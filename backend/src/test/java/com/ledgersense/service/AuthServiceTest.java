package com.ledgersense.service;

import com.ledgersense.domain.AppUser;
import com.ledgersense.domain.Organization;
import com.ledgersense.domain.Role;
import com.ledgersense.factory.AppUserFactory;
import com.ledgersense.factory.OrganizationFactory;
import com.ledgersense.service.impl.IAppUserService;
import com.ledgersense.service.impl.IOrganizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private IOrganizationService organizationService;
    @Mock
    private IAppUserService appUserService;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(organizationService, appUserService, passwordEncoder);
    }

    @Test
    void register() {
        Organization org = OrganizationFactory.createOrganization("Digital Solutions WC", "ZAR");
        AppUser user = AppUserFactory.createAppUser(UUID.randomUUID(), "aidan@digitalsolutions.co.za", "{bcrypt}hash", Role.OWNER);

        Mockito.when(organizationService.create("Digital Solutions WC", "ZAR")).thenReturn(org);
        Mockito.when(passwordEncoder.encode("correct-horse-battery")).thenReturn("{bcrypt}hash");
        // The user must be created with the HASH, not the plain password
        Mockito.when(appUserService.create(Mockito.any(), Mockito.eq("aidan@digitalsolutions.co.za"),
                Mockito.eq("{bcrypt}hash"), Mockito.eq(Role.OWNER))).thenReturn(user);

        AppUser result = authService.register("Digital Solutions WC", "aidan@digitalsolutions.co.za", "correct-horse-battery");

        assertEquals("aidan@digitalsolutions.co.za", result.getEmail());
    }

    // A short password stops everything BEFORE an org is created
    @Test
    void register_whenPasswordTooShort() {
        assertThrows(IllegalArgumentException.class, () -> {
            authService.register("Digital Solutions WC", "aidan@digitalsolutions.co.za", "short");
        });

        Mockito.verify(organizationService, Mockito.never()).create(Mockito.any(), Mockito.any());
    }
}