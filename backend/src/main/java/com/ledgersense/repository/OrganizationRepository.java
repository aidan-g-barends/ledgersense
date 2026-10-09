package com.ledgersense.repository;

import com.ledgersense.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    List<Organization> findByName(String name);

    List<Organization> findByBaseCurrency(String baseCurrency);
}
