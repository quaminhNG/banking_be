package com.banking.modules.account.service;

import com.banking.modules.account.dto.request.CreateAccountRequest;
import com.banking.modules.account.entity.Account;
import com.banking.modules.account.entity.AccountStatus;
import com.banking.modules.account.repository.AccountRepository;
import com.banking.modules.audit.service.AuditService;
import com.banking.modules.ledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final LedgerService ledgerService;
    private final AuditService auditService;

    @Transactional
    public void createAccount(CreateAccountRequest request) {
        String accountId = UUID.randomUUID().toString();

        Account account = new Account();
        account.setId(accountId);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());
        accountRepository.save(account);

        ledgerService.createInitialEntry(accountId, request.getAmount());
        auditService.createAuditLog(accountId);

    }
}