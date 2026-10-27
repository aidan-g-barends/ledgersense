package com.ledgersense.service.impl;

import com.ledgersense.domain.Merchant;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public interface IMerchantService {

    // name -> earliest date seen. Returns name -> Merchant (existing or newly created)
    Map<String, Merchant> findOrCreate(UUID orgId, Map<String, LocalDate> firstSeenByName);

    Merchant read(UUID id, UUID orgId);
}