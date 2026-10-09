package com.ledgersense.repository;

import com.ledgersense.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IAppUserRepository extends JpaRepository <AppUser, UUID> {

    // email is unique, so at most one match: Optional instead of List
    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

}
