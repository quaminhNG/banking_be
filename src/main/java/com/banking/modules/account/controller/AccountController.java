package com.banking.modules.account.controller;
import com.banking.modules.account.dto.request.CreateAccountRequest;
import com.banking.modules.account.dto.response.CreateAccountResponse;
import com.banking.modules.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;
    @PostMapping
    public ResponseEntity<CreateAccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        String accountId = accountService.createAccount(request);
        return ResponseEntity.ok(new CreateAccountResponse(accountId, "Account created successfully"));
    }
}
