package com.ledgersense.repository;

import com.ledgersense.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IAppUserRepository extends JpaRepository <AppUser, UUID> {


    List<AppUser> findsByEmail(String email);

    boolean existsByEmail(String email);

}
