package com.ledgersense.factory;

import com.ledgersense.domain.AuditAction;
import com.ledgersense.domain.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuditEventFactoryTest {

    @Test
    void createAuditEvent() {
        AuditEvent event = AuditEventFactory.createAuditEvent(AuditAction.CATEGORY_CREATED,
                UUID.randomUUID(), UUID.randomUUID(), null, "127.0.0.1", "x".repeat(600));

        assertNotNull(event);
        assertEquals(500, event.getDetail().length());
    }

    @Test
    void createAuditEvent_withoutAction() {
        AuditEvent event = AuditEventFactory.createAuditEvent(null, null, null, null, null, null);

        assertNull(event);
    }
}