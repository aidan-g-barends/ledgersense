package com.ledgersense.factory;

import com.ledgersense.domain.ImportBatch;
import com.ledgersense.domain.ImportStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ImportBatchFactoryTest {

    @Test
    void createImportBatch() {
        ImportBatch batch = ImportBatchFactory.createImportBatch(
                UUID.randomUUID(), UUID.randomUUID(), "C:\\Users\\Aidan\\fnb-october.csv");

        assertNotNull(batch);
        assertEquals("fnb-october.csv", batch.getFileName());
        assertEquals(ImportStatus.PENDING, batch.getStatus());
    }

    @Test
    void createImportBatch_withoutAccount() {
        ImportBatch batch = ImportBatchFactory.createImportBatch(UUID.randomUUID(), null, "fnb.csv");

        assertNull(batch);
    }
}
