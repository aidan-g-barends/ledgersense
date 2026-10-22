package com.ledgersense.controller;

import com.ledgersense.dto.BankTransactionResponse;
import com.ledgersense.dto.PageResponse;
import com.ledgersense.dto.TransactionFilter;
import com.ledgersense.security.CurrentUser;
import com.ledgersense.service.impl.IBankTransactionService;
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

    public BankTransactionController(IBankTransactionService bankTransactionService) {
        this.bankTransactionService = bankTransactionService;
    }

    // Example: GET /api/transactions?page=0&size=50&q=woolworths&from=2026-10-01
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
}