package com.ledgersense.service;

import com.ledgersense.domain.AppUser;
import com.ledgersense.domain.Role;
import com.ledgersense.exception.DuplicateResourceException;
import com.ledgersense.factory.AppUserFactory;
import com.ledgersense.repository.IAppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private IAppUserRepository appUserRepository;

    private AppUserService appUserService;

    private final UUID orgId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        appUserService = new AppUserService(appUserRepository);
    }

    @Test
    void create() {
        Mockito.when(appUserRepository.existsByEmail("aidan@digitalsolutions.co.za")).thenReturn(false);
        Mockito.when(appUserRepository.save(Mockito.any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppUser result = appUserService.create(orgId, "Aidan@DigitalSolutions.co.za", "hashed", Role.OWNER);

        assertEquals("aidan@digitalsolutions.co.za", result.getEmail());
    }

    // NEW: the email is already taken, so it throws and never saves
    @Test
    void create_whenEmailTaken() {
        Mockito.when(appUserRepository.existsByEmail("aidan@digitalsolutions.co.za")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> {
            appUserService.create(orgId, "aidan@digitalsolutions.co.za", "hashed", Role.OWNER);
        });
    }

    @Test
    void findByEmail() {
        AppUser user = AppUserFactory.createAppUser(orgId, "aidan@digitalsolutions.co.za", "hashed", Role.OWNER);
        Mockito.when(appUserRepository.findByEmail("aidan@digitalsolutions.co.za")).thenReturn(Optional.of(user));

        // Capitals on purpose: proves the lookup is case-insensitive
        AppUser result = appUserService.findByEmail("AIDAN@digitalsolutions.co.za");

        assertEquals("aidan@digitalsolutions.co.za", result.getEmail());
    }
}