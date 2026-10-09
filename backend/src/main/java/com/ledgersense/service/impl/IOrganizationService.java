package com.ledgersense.service.impl;

import com.ledgersense.domain.Organization;

import java.util.UUID;

public interface IOrganizationService {

    Organization create(String name, String baseCurrency);

    Organization read(UUID id);

    Organization rename(UUID id, String newName);
}
