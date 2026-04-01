package com.banking.modules.transaction.controller;

import com.banking.modules.transaction.dto.request.TransactionRequest;
import com.banking.modules.transaction.dto.response.TransactionResponse;
import com.banking.modules.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transaction/deposit")
@RequiredArgsConstructor
public class DepositController {
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.processDeposit(request));
    }
}
