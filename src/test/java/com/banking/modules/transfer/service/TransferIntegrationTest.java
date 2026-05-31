package com.banking.modules.transfer.service;

import com.banking.exception.BankingException;
import com.banking.modules.account.entity.Account;
import com.banking.modules.account.entity.AccountStatus;
import com.banking.modules.account.repository.AccountRepository;
import com.banking.modules.auth.entity.User;
import com.banking.modules.ledger.entity.BalanceSnapshot;
import com.banking.modules.ledger.repository.BalanceSnapshotRepository;
import com.banking.modules.ledger.service.LedgerService;
import com.banking.modules.transaction.repository.TransactionRepository;
import com.banking.modules.transfer.dto.request.TransferRequest;
import com.banking.modules.transfer.dto.response.TransferResponse;
import com.banking.modules.transfer.event.TransferCompletedEvent;
import com.banking.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TransferIntegrationTest {

    @Autowired
    private TransferService transferService;

    @SpyBean
    private NotificationService notificationService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BalanceSnapshotRepository balanceSnapshotRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockBean
    private SecurityUtils securityUtils;

    private final String FROM_ACCOUNT_ID = "ACC_INT_1";
    private final String TO_ACCOUNT_ID = "ACC_INT_2";
    private final String USER_EMAIL = "test@gmail.com";

    @BeforeEach
    void setUp() {
        cleanUp();

        Account fromAccount = new Account();
        fromAccount.setId(FROM_ACCOUNT_ID);
        fromAccount.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(fromAccount);

        Account toAccount = new Account();
        toAccount.setId(TO_ACCOUNT_ID);
        toAccount.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(toAccount);

        ledgerService.createInitialEntry(FROM_ACCOUNT_ID, new BigDecimal("1000000.00")); // 1M VND
        ledgerService.createInitialEntry(TO_ACCOUNT_ID, new BigDecimal("0.00"));

        User mockUser = new User();
        mockUser.setId("USER_TEST_INT");
        mockUser.setAccountId(FROM_ACCOUNT_ID);
        mockUser.setEmail(USER_EMAIL);
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
    }

    @AfterEach
    void cleanUp() {
        transactionRepository.deleteAll();
        balanceSnapshotRepository.deleteById(FROM_ACCOUNT_ID);
        balanceSnapshotRepository.deleteById(TO_ACCOUNT_ID);
        accountRepository.deleteById(FROM_ACCOUNT_ID);
        accountRepository.deleteById(TO_ACCOUNT_ID);
    }

    @Test
    void testTransferSuccess_And_KafkaNotificationSent() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(FROM_ACCOUNT_ID);
        request.setToAccountId(TO_ACCOUNT_ID);
        request.setAmount(new BigDecimal("200000.00"));
        request.setCurrency("VND");
        request.setIdempotencyKey(UUID.randomUUID().toString());

        TransferResponse response = transferService.transfer(request);

        assertEquals("SUCCESS", response.getStatus().toString());

        // Verify Balance
        BalanceSnapshot fromSnapshot = balanceSnapshotRepository.findById(FROM_ACCOUNT_ID).orElseThrow();
        BalanceSnapshot toSnapshot = balanceSnapshotRepository.findById(TO_ACCOUNT_ID).orElseThrow();

        assertEquals(0, new BigDecimal("800000.00").compareTo(fromSnapshot.getBalance()));
        assertEquals(0, new BigDecimal("200000.00").compareTo(toSnapshot.getBalance()));

        // Verify Kafka Event Processed by NotificationService
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(notificationService, atLeastOnce()).listenTransferCompletedEvent(any(TransferCompletedEvent.class));
        });
    }

    @Test
    void testTransfer_InsufficientBalance() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(FROM_ACCOUNT_ID);
        request.setToAccountId(TO_ACCOUNT_ID);
        request.setAmount(new BigDecimal("2000000.00")); // More than 1M
        request.setCurrency("VND");
        request.setIdempotencyKey(UUID.randomUUID().toString());

        BankingException exception = assertThrows(BankingException.class, () -> transferService.transfer(request));
        assertEquals("Insufficient balance", exception.getMessage());

        // Verify balances unchanged
        BalanceSnapshot fromSnapshot = balanceSnapshotRepository.findById(FROM_ACCOUNT_ID).orElseThrow();
        assertEquals(0, new BigDecimal("1000000.00").compareTo(fromSnapshot.getBalance()));
    }

    @Test
    void testTransfer_Idempotency() {
        String idempotencyKey = UUID.randomUUID().toString();

        TransferRequest request1 = new TransferRequest();
        request1.setFromAccountId(FROM_ACCOUNT_ID);
        request1.setToAccountId(TO_ACCOUNT_ID);
        request1.setAmount(new BigDecimal("100000.00"));
        request1.setCurrency("VND");
        request1.setIdempotencyKey(idempotencyKey);

        // First transfer
        transferService.transfer(request1);

        // Second transfer with same key
        TransferRequest request2 = new TransferRequest();
        request2.setFromAccountId(FROM_ACCOUNT_ID);
        request2.setToAccountId(TO_ACCOUNT_ID);
        request2.setAmount(new BigDecimal("100000.00"));
        request2.setCurrency("VND");
        request2.setIdempotencyKey(idempotencyKey);

        TransferResponse response2 = transferService.transfer(request2);

        assertEquals("SUCCESS", response2.getStatus().toString());
        assertEquals("Transfer already completed.", response2.getMessage());

        // Verify balance was only deducted ONCE (900,000 left)
        BalanceSnapshot fromSnapshot = balanceSnapshotRepository.findById(FROM_ACCOUNT_ID).orElseThrow();
        assertEquals(0, new BigDecimal("900000.00").compareTo(fromSnapshot.getBalance()));
    }
}
