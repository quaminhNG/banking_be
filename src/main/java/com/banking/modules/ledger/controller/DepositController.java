package com.banking.modules.ledger.controller;

import com.banking.modules.ledger.dto.request.BalanceSnapshotRequest;
import com.banking.modules.ledger.dto.response.BalanceSnapshotResponse;
import com.banking.modules.ledger.service.LedgerService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@AllArgsConstructor

@RestController
@RequestMapping("/api/v1/deposit")

public class DepositController {
    private final LedgerService ledgerService;

    @PostMapping
    public ResponseEntity<BalanceSnapshotResponse> deposit(@RequestBody BalanceSnapshotRequest request) {
        ledgerService.deposit(request);
        return ResponseEntity.ok(new BalanceSnapshotResponse("Successfully deposited " + request.getAmount() + " VND"));
    }

}
