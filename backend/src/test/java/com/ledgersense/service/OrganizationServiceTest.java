package com.ledgersense.service;

import com.ledgersense.domain.Organization;
import com.ledgersense.factory.OrganizationFactory;
import com.ledgersense.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// NEW: turns on Mockito so @Mock works
@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    // NEW: @Mock = a fake repository. No database needed.
    @Mock
    private OrganizationRepository organizationRepository;

    private OrganizationService organizationService;

    // NEW: runs before every test, giving each test a fresh service with the fake repo
    @BeforeEach
    void setUp() {
        organizationService = new OrganizationService(organizationRepository);
    }

    @Test
    void create() {

        Mockito.when(organizationRepository.save(Mockito.any(Organization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        Organization result = organizationService.create("West Coast Solutions", "gbp");

        assertEquals("West Coast Solutions", result.getName());

        assertEquals("GBP", String.valueOf(result.getBaseCurrency()));
    }

    @Test
    void read() {
        UUID id = UUID.randomUUID();
        Organization org = OrganizationFactory.createOrganization("West Coast Solutions", "GBP");

        Mockito.when(organizationRepository.findById(id)).thenReturn(Optional.of(org));

        Organization result = organizationService.read(id);

        assertEquals("West Coast Solutions", result.getName());
    }

    @Test
    void rename() {
        UUID id = UUID.randomUUID();
        Organization org = OrganizationFactory.createOrganization("West Coast Solutions", "GBP");
        Mockito.when(organizationRepository.findById(id)).thenReturn(Optional.of(org));

        Organization result = organizationService.rename(id, "West Coast Holdings");

        assertEquals("West Coast Holdings", result.getName());
    }
}