package com.ledgersense.factory;

import com.ledgersense.domain.AppUser;
import com.ledgersense.domain.Role;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AppUserFactoryTest {

    private final UUID orgId = UUID.randomUUID();

    @Test
    void createAppUser() {
        AppUser user = AppUserFactory.createAppUser(orgId, "  aidan@digitalsolutions.co.za ", "hashed-password", Role.OWNER);

        assertNotNull(user);
        assertEquals("aidan@digitalsolutions.co.za", user.getEmail());
        assertEquals(Role.OWNER, user.getRole());
    }

    @Test
    void createAppUserWhenEmailInvalid() {
        AppUser user = AppUserFactory.createAppUser(orgId, "not-an-email", "hashed-password", Role.OWNER);

        assertNull(user);
    }

    @Test
    void createAppUserWhenOrgMissing() {
        AppUser user = AppUserFactory.createAppUser(null, "aidan@digitalsolutions.co.za", "hashed-password", Role.OWNER);

        assertNull(user);
    }
}