package com.ledgersense.service;

import com.ledgersense.domain.Organization;
import com.ledgersense.exception.ResourceNotFoundException;
import com.ledgersense.factory.OrganizationFactory;
import com.ledgersense.repository.OrganizationRepository;
import com.ledgersense.service.impl.IOrganizationService;
import com.ledgersense.util.Helper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrganizationService implements IOrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository){
        this.organizationRepository = organizationRepository;
    }

    @Override
    @Transactional
    public Organization create(String name, String baseCurrency) {
        Organization organization = OrganizationFactory.createOrganization(name, baseCurrency);

        if (organization == null) {
            throw new IllegalArgumentException("Invalid Organization name or currency");
        }
        return organizationRepository.save(organization);
    }

    @Override
    @Transactional(readOnly = true)
    public Organization read(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + id));
    }

    @Override
    @Transactional
    public Organization rename(UUID id, String newName) {

        if (Helper.isNullOrEmpty(newName) || !Helper.isWithinLength(newName.strip(), 150)) {
            throw new IllegalArgumentException("Organization name must be 1 to 150 characters");
        }

        // Reuse read(): it already throws a 404 if the org doesn't exist
        Organization organization = read(id);
        organization.rename(newName.strip());

        // No save() needed. Inside a transaction, Hibernate notices the change
        // and runs the UPDATE automatically when the method finishes.
        return organization;
    }
}
