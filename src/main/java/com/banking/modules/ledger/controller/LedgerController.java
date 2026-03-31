package com.banking.modules.ledger.controller;

import com.banking.modules.ledger.entity.BalanceSnapshot;
import com.banking.modules.ledger.repository.BalanceSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
public class LedgerController {
    private final BalanceSnapshotRepository balanceSnapshotRepository;

    @GetMapping("/balance/{accountId}")
    public ResponseEntity<BalanceSnapshot> getBalance(@PathVariable String accountId) {
        return balanceSnapshotRepository.findById(accountId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
