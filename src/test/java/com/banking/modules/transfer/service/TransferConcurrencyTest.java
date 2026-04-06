package com.banking.modules.transfer.service;

import com.banking.modules.account.entity.Account;
import com.banking.modules.account.entity.AccountStatus;
import com.banking.modules.account.repository.AccountRepository;
import com.banking.modules.auth.entity.User;
import com.banking.modules.auth.repository.UserRepository;
import com.banking.modules.ledger.entity.BalanceSnapshot;
import com.banking.modules.ledger.repository.BalanceSnapshotRepository;
import com.banking.modules.ledger.service.LedgerService;
import com.banking.modules.transaction.repository.TransactionRepository;
import com.banking.modules.transfer.dto.request.TransferRequest;
import com.banking.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;


import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import lombok.extern.slf4j.Slf4j;

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
    private UserRepository userRepository;

    @Autowired
    private BalanceSnapshotRepository balanceSnapshotRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockBean
    private SecurityUtils securityUtils;

    private final String FROM_ACCOUNT_ID = "ACC_CONCURRENCY_1";
    private final String TO_ACCOUNT_ID = "ACC_CONCURRENCY_2";

    @BeforeEach
    void setUp() {
        // Clean up old data
        cleanUp();

        // 1. Create Accounts
        Account fromAccount = new Account();
        fromAccount.setId(FROM_ACCOUNT_ID);
        fromAccount.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(fromAccount);

        Account toAccount = new Account();
        toAccount.setId(TO_ACCOUNT_ID);
        toAccount.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(toAccount);

        // 2. Initialize Ledgers with balance
        ledgerService.createInitialEntry(FROM_ACCOUNT_ID, new BigDecimal("20000000.00")); // Đủ cho 1000 lượt x 10000 = 10,000,000
        ledgerService.createInitialEntry(TO_ACCOUNT_ID, new BigDecimal("0.00"));

        // 3. Mock SecurityUtils for ownership check
        User mockUser = new User();
        mockUser.setId("USER_TEST_CONCURRENCY");
        mockUser.setAccountId(FROM_ACCOUNT_ID);
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        // Do nothing verifyAccountOwnership (by default mock void do nothing, but we can also mock it just in case)
        // Wait, securityUtils is mocked, we need to mock verifyAccountOwnership? If it's a void method, mock does nothing by default.
    }

    @AfterEach
    void cleanUp() {
        // Clear old database data
        // transactionRepository.deleteAll(); // <-- TÔI ĐÃ COMMENT DÒNG NÀY LẠI ĐỂ GIỮ LẠI TRANSACTIONS TRONG DB!
        // Since foreign keys might exist, we can just delete from snapshot and account
        balanceSnapshotRepository.deleteById(FROM_ACCOUNT_ID);
        balanceSnapshotRepository.deleteById(TO_ACCOUNT_ID);
        accountRepository.deleteById(FROM_ACCOUNT_ID);
        accountRepository.deleteById(TO_ACCOUNT_ID);
    }

    @Test
    void testTransferConcurrency() throws InterruptedException {
        // Lấy số dư ban đầu trước khi chạy Concurrency
        BigDecimal initialFromBalance = balanceSnapshotRepository.findById(FROM_ACCOUNT_ID).orElseThrow().getBalance();
        BigDecimal initialToBalance = balanceSnapshotRepository.findById(TO_ACCOUNT_ID).orElseThrow().getBalance();
        BigDecimal initialTotal = initialFromBalance.add(initialToBalance);

        int numberOfThreads = 50;
        int numberOfRequests = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                try {
                    TransferRequest request = new TransferRequest();
                    request.setFromAccountId(FROM_ACCOUNT_ID);
                    request.setToAccountId(TO_ACCOUNT_ID);
                    request.setAmount(new BigDecimal("10000")); // Mỗi lần chuyển 10000
                    request.setCurrency("VND");
                    request.setIdempotencyKey(UUID.randomUUID().toString()); // Phải random để không bị conflict the same request

                    transferService.transfer(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("Transfer failed: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        BalanceSnapshot fromSnapshot = balanceSnapshotRepository.findById(FROM_ACCOUNT_ID).orElseThrow();
        BalanceSnapshot toSnapshot = balanceSnapshotRepository.findById(TO_ACCOUNT_ID).orElseThrow();

        BigDecimal finalTotal = fromSnapshot.getBalance().add(toSnapshot.getBalance());

        log.info("============== KẾT QUẢ TEST ĐA LUỒNG ==============");
        log.info("Số giao dịch chuyển tiền THÀNH CÔNG: {}", successCount.get());
        log.info("Số giao dịch chuyển tiền THẤT BẠI: {}", failCount.get());
        log.info("Số dư tài khoản chuyển tiền (bị trừ): {} VND", fromSnapshot.getBalance());
        log.info("Số dư tài khoản nhận tiền (được cộng): {} VND", toSnapshot.getBalance());
        log.info("---------------------------------------------------");
        log.info("Tổng tiền hệ thống ban đầu (Initial): {} VND", initialTotal);
        log.info("Tổng tiền hệ thống lúc sau (Final)  : {} VND", finalTotal);
        log.info("===================================================");

        // Assert (Kiểm tra đúng sai tự động báo xanh đỏ trên IDE)
        
        // 🔥 Quan trọng nhất: Bảo toàn tổng khối lượng tiền
        org.junit.jupiter.api.Assertions.assertEquals(0, initialTotal.compareTo(finalTotal), "Tổng tiền ban đầu và lúc sau phải bằng nhau (Bảo toàn khối lượng)");
        
        // 🔥 Không bị âm tiền
        org.junit.jupiter.api.Assertions.assertTrue(fromSnapshot.getBalance().compareTo(BigDecimal.ZERO) >= 0, "Tài khoản nguồn không bị âm tiền");
        org.junit.jupiter.api.Assertions.assertTrue(toSnapshot.getBalance().compareTo(BigDecimal.ZERO) >= 0, "Tài khoản đích không bị âm tiền");

        org.junit.jupiter.api.Assertions.assertEquals(1000, successCount.get(), "Phải có chẩn 1000 giao dịch thành công");
        org.junit.jupiter.api.Assertions.assertEquals(0, failCount.get(), "Không được phép có giao dịch nào thất bại");
        // Kiểm tra số dư cuối cùng
        org.junit.jupiter.api.Assertions.assertEquals(0, new BigDecimal("10000000.00").compareTo(fromSnapshot.getBalance()), "Số dư nguồn phải trừ đúng 10 triệu");
        org.junit.jupiter.api.Assertions.assertEquals(0, new BigDecimal("10000000.00").compareTo(toSnapshot.getBalance()), "Số dư đích phải cộng đúng 10 triệu");

        // Kiểm tra xem luồng có bị lỗi gì không
        // Với khóa bi quan của LedgerService (Pessimistic lock), nó nên tuần tự hóa và hoàn thành được tất cả nếu timeout cho DB đủ lớn.
        // Thực tế có thể có deadlock hoặc timeout exception do tranh chấp khóa.
    }
}
