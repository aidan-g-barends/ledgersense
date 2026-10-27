package com.ledgersense.repository;

import com.ledgersense.domain.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IMerchantRepository extends JpaRepository<Merchant, UUID> {

    // One query for every merchant name in an import (no N+1)
    List<Merchant> findAllByOrgIdAndNameIn(UUID orgId, Collection<String> names);

    Optional<Merchant> findByIdAndOrgId(UUID id, UUID orgId);
}