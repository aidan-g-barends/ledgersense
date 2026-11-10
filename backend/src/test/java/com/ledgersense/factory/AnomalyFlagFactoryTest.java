package com.ledgersense.factory;

import com.ledgersense.domain.AnomalyFlag;
import com.ledgersense.domain.FlagEvidence;
import com.ledgersense.domain.FlagRule;
import com.ledgersense.domain.FlagSeverity;
import com.ledgersense.domain.FlagStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AnomalyFlagFactoryTest {

    private final UUID orgId = UUID.randomUUID();
    private final UUID txId = UUID.randomUUID();
    private final List<FlagEvidence> evidence = List.of(new FlagEvidence("Each charge", "R 299,00"));

    @Test
    void createAnomalyFlag() {
        AnomalyFlag flag = AnomalyFlagFactory.createAnomalyFlag(orgId, txId, FlagRule.DUPLICATE_CHARGE,
                FlagSeverity.CRITICAL, " Possible duplicate charge ", "Charged twice.", evidence);

        assertNotNull(flag);
        assertEquals("Possible duplicate charge", flag.getTitle());
        assertEquals(FlagStatus.OPEN, flag.getStatus());
    }

    @Test
    void createAnomalyFlag_withoutTransaction() {
        AnomalyFlag flag = AnomalyFlagFactory.createAnomalyFlag(orgId, null, FlagRule.DUPLICATE_CHARGE,
                FlagSeverity.CRITICAL, "Possible duplicate charge", "Charged twice.", evidence);

        assertNull(flag);
    }

    @Test
    void createAnomalyFlag_withBlankTitle() {
        AnomalyFlag flag = AnomalyFlagFactory.createAnomalyFlag(orgId, txId, FlagRule.DUPLICATE_CHARGE,
                FlagSeverity.CRITICAL, "   ", "Charged twice.", evidence);

        assertNull(flag);
    }
}