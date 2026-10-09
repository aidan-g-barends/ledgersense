package com.ledgersense.service;

import com.ledgersense.domain.AppUser;
import com.ledgersense.domain.Role;
import com.ledgersense.exception.DuplicateResourceException;
import com.ledgersense.exception.ResourceNotFoundException;
import com.ledgersense.factory.AppUserFactory;
import com.ledgersense.repository.IAppUserRepository;
import com.ledgersense.service.impl.IAppUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class AppUserService implements IAppUserService {

    private final IAppUserRepository appUserRepository;

    public AppUserService(IAppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    @Transactional
    public AppUser create(UUID orgId, String email, String passwordHash, Role role) {
        AppUser user = AppUserFactory.createAppUser(orgId, email, passwordHash, role);

        if (user == null) {
            throw new IllegalArgumentException("Invalid user details");
        }

        // NEW: check AFTER the factory, so we compare the cleaned-up (lowercase) email
        if (appUserRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        return appUserRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AppUser read(UUID id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public AppUser findByEmail(String email) {
        // NEW: clean up the email the SAME way the factory does,
        // or "Aidan@X.com" would never find "aidan@x.com"
        String normalised = email == null ? "" : email.strip().toLowerCase(Locale.ROOT);

        return appUserRepository.findByEmail(normalised)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}