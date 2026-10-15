package com.ledgersense.controller;

import com.ledgersense.domain.BankAccount;
import com.ledgersense.dto.BankAccountResponse;
import com.ledgersense.dto.CreateBankAccountRequest;
import com.ledgersense.dto.RenameBankAccountRequest;
import com.ledgersense.security.CurrentUser;
import com.ledgersense.service.impl.IBankAccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bank-accounts")
public class BankAccountController {

    private final IBankAccountService bankAccountService;

    public BankAccountController(IBankAccountService bankAccountService){
        this.bankAccountService = bankAccountService;
    }
    @PostMapping
    public ResponseEntity<BankAccountResponse> create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateBankAccountRequest request) {
        CurrentUser current = CurrentUser.from(jwt);
        BankAccount account = bankAccountService.create(current.orgId(), request.name(),
                request.bank(), request.accountLast4(), request.currency());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(account.getId()).toUri();
        return ResponseEntity.created(location).body(BankAccountResponse.from(account));
    }

    @GetMapping
    public List<BankAccountResponse> readAll(@AuthenticationPrincipal Jwt jwt) {
        CurrentUser current = CurrentUser.from(jwt);
        return bankAccountService.readAll(current.orgId()).stream()
                .map(BankAccountResponse::from)  // turn each entity into a DTO
                .toList();
    }

    // NOTE: {id} IS in the URL here, unlike /api/organization. That's fine, because
    // the service looks it up WITH the orgId from the token. A wrong-org id = 404.
    @GetMapping("/{id}")
    public BankAccountResponse read(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        CurrentUser current = CurrentUser.from(jwt);
        return BankAccountResponse.from(bankAccountService.read(id, current.orgId()));
    }

    @PatchMapping("/{id}/name")
    public BankAccountResponse rename(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                      @Valid @RequestBody RenameBankAccountRequest request) {
        CurrentUser current = CurrentUser.from(jwt);
        return BankAccountResponse.from(bankAccountService.rename(id, current.orgId(), request.name()));
    }
}
