package com.ledgersense.dto;

import com.ledgersense.domain.ImportBatch;
import com.ledgersense.domain.ImportStatus;

import java.time.Instant;
import java.util.UUID;

public record ImportBatchResponse(UUID id, UUID bankAccountId, String fileName, ImportStatus status,
                                  int rowsTotal, int rowsImported, int rowsSkipped, Instant createdAt) {

    public static ImportBatchResponse from(ImportBatch b) {
        return new ImportBatchResponse(b.getId(), b.getBankAccountId(), b.getFileName(), b.getStatus(),
                b.getRowsTotal(), b.getRowsImported(), b.getRowsSkipped(), b.getCreatedAt());
    }
}