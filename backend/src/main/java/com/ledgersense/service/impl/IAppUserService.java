package com.ledgersense.service.impl;

import com.ledgersense.domain.AppUser;
import com.ledgersense.domain.Role;

import java.util.UUID;

public interface IAppUserService {

    AppUser create(UUID orgId, String email, String passwordHash, Role role);

    AppUser read(UUID id);

    AppUser findByEmail(String email);

}
