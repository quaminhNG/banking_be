package com.banking.modules.transfer.service;

import com.banking.exception.BankingException;
import com.banking.infrastructure.externalbank.ExternalBankManager;
import com.banking.infrastructure.externalbank.ExternalBankProvider;
import com.banking.modules.audit.service.AuditService;
import com.banking.modules.ledger.service.LedgerService;
import com.banking.modules.transaction.entity.Transaction;
import com.banking.modules.transaction.entity.TransactionStatus;
import com.banking.modules.transaction.entity.TransactionType;
import com.banking.modules.transaction.repository.TransactionRepository;
import com.banking.modules.transfer.dto.request.TransferRequest;
import com.banking.modules.transfer.dto.response.TransferResponse;
import com.banking.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalTransferService {

    private final ExternalBankManager bankManager;
    private final LedgerService ledgerService;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    public TransferResponse transferToExternal(TransferRequest request) {
        ExternalBankProvider provider = bankManager.getProvider(request.getToBankCode());

        if (!provider.validateAccount(request.getToAccountId())) {
            throw new BankingException("Target external account not found in " + request.getToBankCode(),
                    HttpStatus.NOT_FOUND);
        }

        String internalTxId = withdrawInternal(request);

        try {
            log.info("Calling external bank API for tx: {}", internalTxId);
            String externalTxId = provider.executeTransfer(
                    request.getToAccountId(),
                    request.getAmount(),
                    request.getCurrency(),
                    request.getIdempotencyKey());

            updateTransactionStatus(internalTxId, TransactionStatus.SUCCESS, externalTxId);

            auditService.createAuditLog(internalTxId, securityUtils.getCurrentUser().getId(),
                    "EXTERNAL_TRANSFER", "To: " + request.getToAccountId() + " via " + request.getToBankCode());

            TransferResponse response = new TransferResponse();
            response.setTransactionId(internalTxId);
            response.setFromAccountId(request.getFromAccountId());
            response.setToAccountId(request.getToAccountId());
            response.setAmount(request.getAmount());
            response.setCurrency(request.getCurrency());
            response.setStatus(TransactionStatus.SUCCESS);
            response.setMessage("External transfer successful. External ID: " + externalTxId);
            response.setCreatedAt(LocalDateTime.now());
            return response;

        } catch (Exception e) {
            log.error("External bank transfer failed for {}. Refunding user...", internalTxId, e);
            refundInternal(request, internalTxId);
            throw new BankingException("External bank rejected the transaction. Money has been refunded.",
                    HttpStatus.BAD_GATEWAY);
        }
    }

    @Transactional
    protected String withdrawInternal(TransferRequest request) {
        String txId = UUID.randomUUID().toString();
        Transaction tx = new Transaction();
        tx.setId(txId);
        tx.setFromAccountId(request.getFromAccountId());
        tx.setToAccountId(request.getToAccountId());
        tx.setAmount(request.getAmount());
        tx.setCurrency(request.getCurrency());
        tx.setType(TransactionType.EXTERNAL_TRANSFER);
        tx.setStatus(TransactionStatus.PENDING);
        tx.setIdempotencyKey(request.getIdempotencyKey());
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(tx);

        ledgerService.withdraw(request.getFromAccountId(), request.getAmount(), txId);
        return txId;
    }

    @Transactional
    protected void refundInternal(TransferRequest request, String txId) {
        ledgerService.deposit(request.getFromAccountId(), request.getAmount(), "REFUND-" + txId);
        updateTransactionStatus(txId, TransactionStatus.FAILED, null);
    }

    @Transactional
    protected void updateTransactionStatus(String txId, TransactionStatus status, String externalId) {
        transactionRepository.findById(txId).ifPresent(tx -> {
            tx.setStatus(status);
            tx.setUpdatedAt(LocalDateTime.now());

            transactionRepository.save(tx);
        });
    }
}
