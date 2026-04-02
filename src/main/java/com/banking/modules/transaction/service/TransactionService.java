package com.banking.modules.transaction.service;

import com.banking.common.constants.TransactionConstants;
import com.banking.modules.ledger.service.LedgerService;
import com.banking.modules.transaction.dto.request.TransactionRequest;
import com.banking.modules.transaction.dto.response.TransactionResponse;
import com.banking.modules.transaction.entity.Transaction;
import com.banking.modules.transaction.entity.TransactionStatus;
import com.banking.modules.transaction.entity.TransactionType;
import com.banking.modules.transaction.repository.TransactionRepository;
import com.banking.security.SecurityUtils;
import com.banking.modules.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final LedgerService ledgerService;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;

    @Transactional
    public TransactionResponse processDeposit(TransactionRequest request) {
        if (request.getIdempotencyKey() != null) {
            Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                return mapToResponse(existing.get(), "Transaction already processed (Idempotent)");
            }
        }
        return processTransaction(request, TransactionType.DEPOSIT);
    }

    @Transactional
    public TransactionResponse processWithdraw(TransactionRequest request) {
        if (request.getIdempotencyKey() != null) {
            Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                return mapToResponse(existing.get(), "Transaction already processed (Idempotent)");
            }
        }
        return processTransaction(request, TransactionType.WITHDRAW);
    }

    private TransactionResponse processTransaction(TransactionRequest request, TransactionType type) {
        // Chỉ cho phép thao tác trên account của chính mình
        securityUtils.verifyAccountOwnership(request.getAccountId());
        if (request.getIdempotencyKey() != null) {
            Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                return mapToResponse(existing.get(), "Duplicate request detected.");
            }
        }

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setAccountId(request.getAccountId());
        transaction.setAmount(request.getAmount());
        transaction.setType(type);
        transaction.setCurrency(TransactionConstants.CURRENCY_VND);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setIdempotencyKey(request.getIdempotencyKey());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);

        if (type == TransactionType.DEPOSIT) {
            ledgerService.deposit(transaction.getAccountId(), transaction.getAmount(), transaction.getId());
        } else if (type == TransactionType.WITHDRAW) {
            ledgerService.withdraw(transaction.getAccountId(), transaction.getAmount(), transaction.getId());
        }

        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        // Audit Logging
        String userId = securityUtils.getCurrentUser().getId();
        auditService.createAuditLog(
                transaction.getId(),
                userId,
                type.name(),
                "{\"amount\":" + transaction.getAmount() + ",\"accountId\":\"" + transaction.getAccountId() + "\"}");

        return mapToResponse(transaction, "Transaction successful.");
    }

    private TransactionResponse mapToResponse(Transaction transaction, String message) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getAmount(),
                transaction.getType().name(),
                transaction.getStatus(),
                message,
                transaction.getCreatedAt());
    }
}
