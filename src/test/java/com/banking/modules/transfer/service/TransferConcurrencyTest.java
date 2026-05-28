package com.banking.modules.transfer.service;

import com.banking.modules.account.entity.Account;
import com.banking.modules.account.entity.AccountStatus;
import com.banking.modules.account.repository.AccountRepository;
import com.banking.modules.auth.entity.User;
import com.banking.security.SecurityUtils;
import com.banking.modules.ledger.entity.BalanceSnapshot;
import com.banking.modules.ledger.repository.BalanceSnapshotRepository;
import com.banking.modules.ledger.service.LedgerService;
import com.banking.modules.transaction.repository.TransactionRepository;
import com.banking.modules.transfer.dto.request.TransferRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
public class TransferConcurrencyTest {

    @Autowired
    private TransferService transferService;

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

    @MockBean
    private com.banking.modules.transaction.controller.DepositController depositController;

    private final String FROM_ACCOUNT_ID = "ACC_CONCURRENCY_1";
    private final String TO_ACCOUNT_ID = "ACC_CONCURRENCY_2";

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

        ledgerService.createInitialEntry(FROM_ACCOUNT_ID, new BigDecimal("20000000.00"));
        ledgerService.createInitialEntry(TO_ACCOUNT_ID, new BigDecimal("0.00"));

        User mockUser = new User();
        mockUser.setId("USER_TEST_CONCURRENCY");
        mockUser.setAccountId(FROM_ACCOUNT_ID);
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
    void testTransferConcurrency() throws InterruptedException {
        BigDecimal initialTotal = new BigDecimal("20000000.00");

        int numberOfThreads = 20;
        int numberOfRequests = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        log.info("Bắt đầu thực hiện {} giao dịch đồng thời...", numberOfRequests);

        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                try {
                    TransferRequest request = new TransferRequest();
                    request.setFromAccountId(FROM_ACCOUNT_ID);
                    request.setToAccountId(TO_ACCOUNT_ID);
                    request.setAmount(new BigDecimal("10000"));
                    request.setCurrency("VND");
                    request.setIdempotencyKey(UUID.randomUUID().toString());

                    transferService.transfer(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.error("Giao dịch thất bại: {}", e.getMessage());
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        boolean finished = executor.awaitTermination(2, TimeUnit.MINUTES);
        assertTrue(finished, "Test timeout trước khi hoàn thành");

        BalanceSnapshot fromSnapshot = balanceSnapshotRepository.findById(FROM_ACCOUNT_ID).orElseThrow();
        BalanceSnapshot toSnapshot = balanceSnapshotRepository.findById(TO_ACCOUNT_ID).orElseThrow();
        BigDecimal finalTotal = fromSnapshot.getBalance().add(toSnapshot.getBalance());

        log.info("============== KẾT QUẢ FINAL ==============");
        log.info("Thành công: {} | Thất bại: {}", successCount.get(), failCount.get());
        log.info("Tổng tiền ban đầu: {} | Tổng tiền hiện tại: {}", initialTotal, finalTotal);

        log.info("Đang đợi các worker xử lý ghi log giao dịch...");
        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    long totalTx = transactionRepository.count();
                    assertEquals(1000, totalTx, "Số lượng bản ghi Transaction không khớp (Không dùng RabbitMQ)");
                });


        assertEquals(0, initialTotal.compareTo(finalTotal), "Tổng tiền hệ thống không được thay đổi!");
        assertEquals(1000, successCount.get(), "Số lượng giao dịch thành công không đạt 1000");
        assertEquals(0, new BigDecimal("10000000.00").compareTo(fromSnapshot.getBalance()), "Số dư nguồn sai");
        assertEquals(0, new BigDecimal("10000000.00").compareTo(toSnapshot.getBalance()), "Số dư đích sai");
    }
}