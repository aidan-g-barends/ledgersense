package com.ledgersense.controller;

import com.ledgersense.domain.AuditAction;
import com.ledgersense.dto.*;
import com.ledgersense.security.CurrentUser;
import com.ledgersense.service.impl.IAuditService;
import com.ledgersense.service.impl.IBankTransactionService;
import com.ledgersense.service.impl.IBankTransactionService.RecategoriseResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class BankTransactionController {

    private final IBankTransactionService bankTransactionService;
    private final IAuditService auditService; // NEW

    public BankTransactionController(IBankTransactionService bankTransactionService, IAuditService auditService) {
        this.bankTransactionService = bankTransactionService;
        this.auditService = auditService;
    }

    @GetMapping
    public PageResponse<BankTransactionResponse> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) UUID bankAccountId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        CurrentUser current = CurrentUser.from(jwt);
        TransactionFilter filter = new TransactionFilter(bankAccountId, categoryId, from, to, q);
        return PageResponse.from(
                bankTransactionService.page(current.orgId(), filter, page, size),
                BankTransactionResponse::from);
    }

    @GetMapping("/{id}")
    public BankTransactionResponse read(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        CurrentUser current = CurrentUser.from(jwt);
        return BankTransactionResponse.from(bankTransactionService.read(id, current.orgId()));
    }

    // NEW
    @PatchMapping("/{id}/category")
    public RecategoriseResponse recategorise(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                             @RequestBody RecategoriseRequest request) {
        CurrentUser current = CurrentUser.from(jwt);
        RecategoriseResult result = bankTransactionService.recategorise(
                id, current.orgId(), request.categoryId(), request.rememberMerchant());

        auditService.record(AuditAction.TRANSACTION_RECATEGORISED, current.orgId(), current.id(), id,
                request.rememberMerchant()
                        ? "Applied to merchant (" + result.merchantTransactionsUpdated() + " transactions)"
                        : null);

        return RecategoriseResponse.from(result);
    }
}