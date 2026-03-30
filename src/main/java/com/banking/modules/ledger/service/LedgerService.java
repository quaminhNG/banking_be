package com.banking.modules.ledger.service;

import com.banking.modules.ledger.dto.request.BalanceSnapshotRequest;
import com.banking.modules.ledger.entity.BalanceSnapshot;
import com.banking.modules.ledger.entity.LedgerEntry;
import com.banking.modules.ledger.entity.LedgerType;
import com.banking.modules.ledger.repository.BalanceSnapshotRepository;
import com.banking.modules.ledger.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {
    private final LedgerRepository ledgerRepository;
    private final BalanceSnapshotRepository balanceSnapshotRepository;

    @Transactional
    public void createInitialEntry(String accountId, BigDecimal amount) {
        BigDecimal initialAmount = (amount != null) ? amount : BigDecimal.ZERO;

        LedgerEntry entry = new LedgerEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setAccountId(accountId);
        entry.setType(LedgerType.CREDIT);
        entry.setAmount(initialAmount);
        entry.setCurrency("VND");
        entry.setReferenceId("INITIAL_DEPOSIT");
        entry.setCreatedAt(LocalDateTime.now());
        ledgerRepository.save(entry);

        BalanceSnapshot balanceSnapshot = new BalanceSnapshot();
        balanceSnapshot.setAccountId(accountId);
        balanceSnapshot.setBalance(initialAmount);
        balanceSnapshot.setVersion(0);
        balanceSnapshot.setUpdatedAt(LocalDateTime.now());
        balanceSnapshotRepository.save(balanceSnapshot);
    }

    @Transactional
    public void deposit(BalanceSnapshotRequest request) {
        deposit(request.getAccountId(), request.getAmount(), "DEPOSIT_API");
    }

    @Transactional
    public void deposit(String accountId, BigDecimal amount, String referenceId) {
        LedgerEntry entry = new LedgerEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setAccountId(accountId);
        entry.setType(LedgerType.CREDIT);
        entry.setAmount(amount);
        entry.setCurrency("VND");
        entry.setReferenceId(referenceId);
        entry.setCreatedAt(LocalDateTime.now());
        ledgerRepository.save(entry);

        BalanceSnapshot balanceSnapshot = balanceSnapshotRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new RuntimeException("The account has not been initialized with a balance!"));

        BigDecimal newBalance = balanceSnapshot.getBalance().add(amount);
        balanceSnapshot.setBalance(newBalance);
        balanceSnapshot.setUpdatedAt(LocalDateTime.now());
        balanceSnapshotRepository.save(balanceSnapshot);
    }

    @Transactional
    public void withdraw(BalanceSnapshotRequest request) {
        withdraw(request.getAccountId(), request.getAmount(), "WITHDRAW_API");
    }

    @Transactional
    public void withdraw(String accountId, BigDecimal amount, String referenceId) {
        LedgerEntry entry = new LedgerEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setAccountId(accountId);
        entry.setType(LedgerType.DEBIT);
        entry.setAmount(amount);
        entry.setCurrency("VND");
        entry.setReferenceId(referenceId);
        entry.setCreatedAt(LocalDateTime.now());
        ledgerRepository.save(entry);

        BalanceSnapshot balanceSnapshot = balanceSnapshotRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new RuntimeException("The account has not been initialized with a balance!"));

        BigDecimal newBalance = balanceSnapshot.getBalance().subtract(amount);
        balanceSnapshot.setBalance(newBalance);
        balanceSnapshot.setUpdatedAt(LocalDateTime.now());
        balanceSnapshotRepository.save(balanceSnapshot);
    }
}
