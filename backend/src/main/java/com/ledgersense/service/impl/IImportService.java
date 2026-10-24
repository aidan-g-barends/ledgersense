package com.ledgersense.service.impl;

import com.ledgersense.importer.ColumnMapping;
import com.ledgersense.importer.ParseResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface IImportService {

    // Parses and validates, saves NOTHING
    ParseResult preview(UUID orgId, UUID bankAccountId, MultipartFile file, ColumnMapping mapping);
}