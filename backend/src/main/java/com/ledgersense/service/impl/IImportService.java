package com.ledgersense.service.impl;

import com.ledgersense.domain.ImportBatch;
import com.ledgersense.importer.ColumnMapping;
import com.ledgersense.importer.ImportResult;
import com.ledgersense.importer.ParseResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IImportService {

    // mapping may be null: then the bank account's preset is used
    ParseResult preview(UUID orgId, UUID bankAccountId, MultipartFile file, ColumnMapping mapping);

    // NEW: parses, skips duplicates, auto-categorises and SAVES
    ImportResult commit(UUID orgId, UUID bankAccountId, MultipartFile file, ColumnMapping mapping);

    List<ImportBatch> history(UUID orgId); // NEW
}