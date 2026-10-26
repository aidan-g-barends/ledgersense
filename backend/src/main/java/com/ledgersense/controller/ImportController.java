package com.ledgersense.controller;

import com.ledgersense.domain.AuditAction;
import com.ledgersense.dto.ColumnMappingRequest;
import com.ledgersense.dto.ImportPreviewResponse;
import com.ledgersense.importer.ImportResult;
import com.ledgersense.security.CurrentUser;
import com.ledgersense.service.impl.IAuditService;
import com.ledgersense.service.impl.IImportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/imports")
public class ImportController {

    private final IImportService importService;
    private final IAuditService auditService; // NEW

    public ImportController(IImportService importService, IAuditService auditService) {
        this.importService = importService;
        this.auditService = auditService;
    }

    @PostMapping(path = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportPreviewResponse preview(@AuthenticationPrincipal Jwt jwt,
                                         @RequestParam("file") MultipartFile file,
                                         @RequestParam UUID bankAccountId,
                                         @Valid @ModelAttribute ColumnMappingRequest mapping) {
        CurrentUser current = CurrentUser.from(jwt);
        // CHANGED: no mapping sent = null = service uses the bank's preset
        return ImportPreviewResponse.from(importService.preview(
                current.orgId(), bankAccountId, file, mapping.isProvided() ? mapping.toMapping() : null));
    }

    // NEW: same inputs as preview, but this one SAVES. 201 Created = new rows exist now.
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> commit(@AuthenticationPrincipal Jwt jwt,
                                               @RequestParam("file") MultipartFile file,
                                               @RequestParam UUID bankAccountId,
                                               @Valid @ModelAttribute ColumnMappingRequest mapping) {
        CurrentUser current = CurrentUser.from(jwt);
        ImportResult result = importService.commit(
                current.orgId(), bankAccountId, file, mapping.isProvided() ? mapping.toMapping() : null);

        auditService.record(AuditAction.IMPORT_COMPLETED, current.orgId(), current.id(), result.batchId(),
                result.fileName() + ": " + result.imported() + " imported, " + result.duplicates() + " duplicates");

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}