package com.ledgersense.service;

import com.ledgersense.domain.Merchant;
import com.ledgersense.exception.ResourceNotFoundException;
import com.ledgersense.factory.MerchantFactory;
import com.ledgersense.repository.IMerchantRepository;
import com.ledgersense.service.impl.IMerchantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class MerchantService implements IMerchantService {

    private final IMerchantRepository merchantRepository;

    public MerchantService(IMerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Override
    @Transactional
    public Map<String, Merchant> findOrCreate(UUID orgId, Map<String, LocalDate> firstSeenByName) {
        Map<String, Merchant> result = new HashMap<>();
        if (firstSeenByName.isEmpty()) {
            return result;
        }
        // 1 query: which of these names do we already know?
        for (Merchant existing : merchantRepository.findAllByOrgIdAndNameIn(orgId, firstSeenByName.keySet())) {
            result.put(existing.getName(), existing);
        }
        // Create only the missing ones, then save them all in one go
        List<Merchant> created = new ArrayList<>();
        for (Map.Entry<String, LocalDate> entry : firstSeenByName.entrySet()) {
            if (!result.containsKey(entry.getKey())) {
                Merchant merchant = MerchantFactory.createMerchant(orgId, entry.getKey(), entry.getValue());
                if (merchant != null) {
                    created.add(merchant);
                }
            }
        }
        for (Merchant saved : merchantRepository.saveAll(created)) {
            result.put(saved.getName(), saved);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Merchant read(UUID id, UUID orgId) {
        return merchantRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));
    }
}