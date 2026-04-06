package com.banking.modules.transfer.service;

import com.banking.exception.BankingException;
import com.banking.modules.audit.service.AuditService;
import com.banking.modules.ledger.service.LedgerService;
import com.banking.modules.transaction.entity.Transaction;
import com.banking.modules.transaction.repository.TransactionRepository;
import com.banking.modules.transfer.dto.request.TransferRequest;
import com.banking.modules.transfer.dto.response.TransferResponse;
import com.banking.modules.auth.entity.User;
import com.banking.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private LedgerService ledgerService;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TransferService transferService;

    private TransferRequest validRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        validRequest = new TransferRequest();
        validRequest.setFromAccountId("ACC123");
        validRequest.setToAccountId("ACC456");
        validRequest.setAmount(new BigDecimal("1000"));
        validRequest.setCurrency("VND");
        validRequest.setIdempotencyKey("unique-key-123");

        mockUser = new User();
        mockUser.setId("USER1");
        mockUser.setAccountId("ACC123");
    }

    @Test
    void transfer_Success() {
        when(transactionRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        TransferResponse response = transferService.transfer(validRequest);

        assertNotNull(response);
        assertEquals("ACC123", response.getFromAccountId());
        assertEquals("ACC456", response.getToAccountId());
        verify(ledgerService, times(1)).transferLedger(anyString(), anyString(), any(BigDecimal.class), anyString());
        verify(auditService, times(1)).createAuditLog(any(), anyString(), eq("TRANSFER"), anyString());
    }

    @Test
    void transfer_ToSelf_ShouldThrowException() {
        validRequest.setToAccountId("ACC123");

        BankingException exception = assertThrows(BankingException.class, () -> {
            transferService.transfer(validRequest);
        });

        assertEquals("Can't transfer to yourself!", exception.getMessage());
        verify(ledgerService, never()).transferLedger(any(), any(), any(), any());
    }

    @Test
    void transfer_InsufficientBalance_ShouldBubbleUpException() {
        when(transactionRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());

        doThrow(new BankingException("Insufficient balance"))
                .when(ledgerService).transferLedger(anyString(), anyString(), any(BigDecimal.class), anyString());

        assertThrows(BankingException.class, () -> {
            transferService.transfer(validRequest);
        });

        verify(auditService, never()).createAuditLog(any(), any(), any(), any());
    }
}
