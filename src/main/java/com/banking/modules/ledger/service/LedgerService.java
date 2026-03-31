package com.banking.modules.ledger.service;

import com.banking.common.constants.LedgerConstants;
import com.banking.common.constants.TransactionConstants;
import com.banking.exception.BankingException;
import com.banking.modules.account.repository.AccountRepository;
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
    private final AccountRepository accountRepository;

    @Transactional
    public void createInitialEntry(String accountId, BigDecimal amount) {
        if (balanceSnapshotRepository.existsById(accountId)) {
            throw new BankingException("Account already initialized");
        }
        BigDecimal initialAmount = (amount != null) ? amount : BigDecimal.ZERO;

        saveLedgerEntry(accountId, LedgerType.CREDIT, initialAmount, LedgerConstants.INITIAL_DEPOSIT);

        BalanceSnapshot balanceSnapshot = new BalanceSnapshot();
        balanceSnapshot.setAccountId(accountId);
        balanceSnapshot.setBalance(initialAmount);
        balanceSnapshot.setVersion(0);
        balanceSnapshot.setUpdatedAt(LocalDateTime.now());
        balanceSnapshotRepository.save(balanceSnapshot);
    }

    @Transactional
    public void deposit(BalanceSnapshotRequest request) {
        accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new BankingException("Account not found"));
        if (request.getAmount().compareTo(TransactionConstants.MIN_DEPOSIT) < 0) {
            throw new BankingException("The minimum deposit amount is " + TransactionConstants.MIN_DEPOSIT
                    + TransactionConstants.CURRENCY_VND);
        }
        deposit(request.getAccountId(), request.getAmount(), LedgerConstants.DEPOSIT_API);
    }

    public void deposit(String accountId, BigDecimal amount, String referenceId) {
        BalanceSnapshot balanceSnapshot = balanceSnapshotRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new BankingException("The account has not been initialized with a balance!"));
        saveLedgerEntry(accountId, LedgerType.CREDIT, amount, referenceId);
        BigDecimal newBalance = balanceSnapshot.getBalance().add(amount);
        balanceSnapshot.setBalance(newBalance);
        balanceSnapshot.setUpdatedAt(LocalDateTime.now());
        balanceSnapshotRepository.save(balanceSnapshot);
    }

    @Transactional
    public void withdraw(BalanceSnapshotRequest request) {
        accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new BankingException("Account not found"));
        if (request.getAmount().compareTo(TransactionConstants.MIN_WITHDRAW) < 0) {
            throw new BankingException("The minimum withdrawal amount is " + TransactionConstants.MIN_WITHDRAW
                    + TransactionConstants.CURRENCY_VND);
        } else if (request.getAmount().compareTo(TransactionConstants.MAX_WITHDRAW) > 0) {
            throw new BankingException("The maximum withdrawal amount is " + TransactionConstants.MAX_WITHDRAW
                    + TransactionConstants.CURRENCY_VND);
        }
        withdraw(request.getAccountId(), request.getAmount(), LedgerConstants.WITHDRAW_API);
    }

    public void withdraw(String accountId, BigDecimal amount, String referenceId) {
        BalanceSnapshot balanceSnapshot = balanceSnapshotRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new BankingException("The account has not been initialized with a balance!"));
        if (balanceSnapshot.getBalance().compareTo(amount) < 0) {
            throw new BankingException("Insufficient balance");
        }
        saveLedgerEntry(accountId, LedgerType.DEBIT, amount, referenceId);

        BigDecimal newBalance = balanceSnapshot.getBalance().subtract(amount);
        balanceSnapshot.setBalance(newBalance);
        balanceSnapshot.setUpdatedAt(LocalDateTime.now());
        balanceSnapshotRepository.save(balanceSnapshot);
    }

    private void saveLedgerEntry(String accountId, LedgerType type, BigDecimal amount, String referenceId) {
        LedgerEntry entry = new LedgerEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setAccountId(accountId);
        entry.setType(type);
        entry.setAmount(amount);
        entry.setCurrency(TransactionConstants.CURRENCY_VND);
        entry.setReferenceId(referenceId);
        entry.setCreatedAt(LocalDateTime.now());
        ledgerRepository.save(entry);
    }
}
