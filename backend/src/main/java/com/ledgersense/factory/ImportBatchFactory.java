package com.ledgersense.factory;

import com.ledgersense.domain.ImportBatch;
import com.ledgersense.domain.ImportStatus;
import com.ledgersense.util.Helper;

import java.util.UUID;

public class ImportBatchFactory {

    public static ImportBatch createImportBatch(UUID orgId, UUID bankAccountId, String fileName) {
        if (orgId == null || bankAccountId == null || Helper.isNullOrBlank(fileName)) {
            return null;
        }
        // WHY: some browsers send a full path like "C:\Users\Aidan\fnb.csv".
        // Keep only the name after the last slash. Never store someone's folder structure.
        String cleanName = fileName.strip();
        int lastSlash = Math.max(cleanName.lastIndexOf('/'), cleanName.lastIndexOf('\\'));
        cleanName = cleanName.substring(lastSlash + 1);

        if (cleanName.isBlank() || !Helper.isWithinLength(cleanName, 255)) {
            return null;
        }

        return new ImportBatch.Builder()
                .setOrgId(orgId)
                .setBankAccountId(bankAccountId)
                .setFileName(cleanName)
                .setStatus(ImportStatus.PENDING) // every batch starts PENDING
                .build();
    }
}
