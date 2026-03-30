package com.banking.modules.transaction.service;

import com.banking.modules.ledger.service.LedgerService;
import com.banking.modules.transaction.dto.request.TransactionRequest;
import com.banking.modules.transaction.dto.response.TransactionResponse;
import com.banking.modules.transaction.entity.Transaction;
import com.banking.modules.transaction.entity.TransactionStatus;
import com.banking.modules.transaction.entity.TransactionType;
import com.banking.modules.transaction.repository.TransactionRepository;
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

    @Transactional
    public TransactionResponse processDeposit(TransactionRequest request) {
        return processTransaction(request, TransactionType.DEPOSIT);
    }

    @Transactional
    public TransactionResponse processWithdraw(TransactionRequest request) {
        return processTransaction(request, TransactionType.WITHDRAW);
    }

    private TransactionResponse processTransaction(TransactionRequest request, TransactionType type) {
        // Idempotency check dup
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
        transaction.setCurrency("VND");
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setIdempotencyKey(request.getIdempotencyKey());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);

        try {
            if (type == TransactionType.DEPOSIT) {
                ledgerService.deposit(transaction.getAccountId(), transaction.getAmount(), transaction.getId());
            } else if (type == TransactionType.WITHDRAW) {
                ledgerService.withdraw(transaction.getAccountId(), transaction.getAmount(), transaction.getId());
            }

            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            return mapToResponse(transaction, "Transaction successful.");
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            return mapToResponse(transaction, "Transaction failed: " + e.getMessage());
        }
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
