package com.ledgersense.controller;

import com.ledgersense.dto.ColumnMappingRequest;
import com.ledgersense.dto.ImportPreviewResponse;
import com.ledgersense.security.CurrentUser;
import com.ledgersense.service.impl.IImportService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/imports")
public class ImportController {

    private final IImportService importService;

    public ImportController(IImportService importService) {
        this.importService = importService;
    }

    // NEW: consumes = multipart/form-data -> a file + normal form fields in one request.
    // @ModelAttribute fills ColumnMappingRequest from the form fields (delimiter, dateColumn, ...).
    @PostMapping(path = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportPreviewResponse preview(@AuthenticationPrincipal Jwt jwt,
                                         @RequestParam("file") MultipartFile file,
                                         @RequestParam UUID bankAccountId,
                                         @Valid @ModelAttribute ColumnMappingRequest mapping) {
        CurrentUser current = CurrentUser.from(jwt);
        return ImportPreviewResponse.from(
                importService.preview(current.orgId(), bankAccountId, file, mapping.toMapping()));
    }
}