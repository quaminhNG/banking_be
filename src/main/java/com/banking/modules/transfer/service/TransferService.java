package com.banking.modules.transfer.service;

import com.banking.common.constants.TransactionConstants;
import com.banking.common.constants.TransferConstants;
import com.banking.exception.BankingException;
import com.banking.modules.ledger.service.LedgerService;
import com.banking.modules.transaction.entity.Transaction;
import com.banking.modules.transaction.entity.TransactionStatus;
import com.banking.modules.transaction.entity.TransactionType;
import com.banking.modules.transaction.repository.TransactionRepository;
import com.banking.modules.transfer.dto.request.TransferRequest;
import com.banking.modules.transfer.dto.response.TransferResponse;
import com.banking.security.SecurityUtils;
import com.banking.modules.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransactionRepository transactionRepository;
    private final LedgerService ledgerService;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        // Chỉ được transfer từ account của chính mình
        securityUtils.verifyAccountOwnership(request.getFromAccountId());
        if (request.getAmount().compareTo(TransferConstants.MIN_TRANSFER) < 0) {
            throw new BankingException("The minimum transfer amount is "
                    + TransferConstants.MIN_TRANSFER + TransactionConstants.CURRENCY_VND);
        }
        if (request.getToAccountId().equals(request.getFromAccountId())) {
            throw new BankingException("Can't transfer to yourself!");
        }

        String incomingHash = hashRequest(request);
        Optional<Transaction> existingTransaction = transactionRepository
                .findByIdempotencyKey(request.getIdempotencyKey());

        if (existingTransaction.isPresent()) {
            Transaction tx = existingTransaction.get();
            // cùng key, != request => conflic
            if (!incomingHash.equals(tx.getRequestHash())) {
                throw new BankingException("Idempotency Key conflict: request parameters changed");
            }
            if (TransactionStatus.SUCCESS.equals(tx.getStatus())) {
                return mapToResponse(tx, "Transfer already completed.");
            }
            if (TransactionStatus.PENDING.equals(tx.getStatus())) {
                throw new BankingException("Transfer is processing...");
            }
        }

        Transaction transaction = existingTransaction.orElseGet(() -> {
            Transaction newTx = new Transaction();
            newTx.setId(UUID.randomUUID().toString());
            newTx.setIdempotencyKey(request.getIdempotencyKey());
            newTx.setRequestHash(incomingHash);
            newTx.setFromAccountId(request.getFromAccountId());
            newTx.setToAccountId(request.getToAccountId());
            newTx.setAmount(request.getAmount());
            newTx.setCurrency(TransactionConstants.CURRENCY_VND);
            newTx.setType(TransactionType.TRANSFER);
            newTx.setStatus(TransactionStatus.PENDING);
            newTx.setCreatedAt(LocalDateTime.now());
            return transactionRepository.save(newTx);
        });

        // 4. Thực hiện ghi sổ cái (Ledger)
        // withdraw từ tài khoản nguồn, deposit vào tài khoản đích
        // LedgerService tự lock theo thứ tự để tránh deadlock
        ledgerService.transferLedger(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount(),
                transaction.getId());

        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        // Audit Logging
        String userId = securityUtils.getCurrentUser().getId();
        auditService.createAuditLog(
            transaction.getId(),
            userId,
            "TRANSFER",
            "{\"amount\":" + transaction.getAmount() + ",\"from\":\"" + request.getFromAccountId() + "\",\"to\":\"" + request.getToAccountId() + "\"}"
        );

        return mapToResponse(transaction, "Transfer successful.");
    }

    // Helper hash check conflic
    private String hashRequest(TransferRequest request) {
        String raw = request.getFromAccountId()
                + "|" + request.getToAccountId()
                + "|" + request.getAmount().toPlainString()
                + "|" + request.getCurrency();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new BankingException("Cannot generate request hash");
        }
    }

    private TransferResponse mapToResponse(Transaction tx, String message) {
        return new TransferResponse(
                tx.getId(),
                tx.getFromAccountId(),
                tx.getToAccountId(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getStatus(),
                message,
                tx.getCreatedAt());
    }
}
