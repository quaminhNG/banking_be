package com.banking.config;

import com.banking.modules.account.entity.Account;
import com.banking.modules.account.entity.AccountStatus;
import com.banking.modules.account.repository.AccountRepository;
import com.banking.modules.auth.entity.User;
import com.banking.modules.auth.entity.UserRole;
import com.banking.modules.auth.repository.UserRepository;
import com.banking.modules.ledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final LedgerService ledgerService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            System.out.println("====== BẮT ĐẦU TẠO DỮ LIỆU MẪU (DATA SEEDING) ======");
            String accountAId = "ACC_SEED_A";
            String accountBId = "ACC_SEED_B";

            Account accountA = new Account();
            accountA.setId(accountAId);
            accountA.setStatus(AccountStatus.ACTIVE);
            accountA.setCreatedAt(LocalDateTime.now());
            accountRepository.save(accountA);

            Account accountB = new Account();
            accountB.setId(accountBId);
            accountB.setStatus(AccountStatus.ACTIVE);
            accountB.setCreatedAt(LocalDateTime.now());
            accountRepository.save(accountB);

            ledgerService.createInitialEntry(accountAId, new BigDecimal("50000000.00"));
            ledgerService.createInitialEntry(accountBId, new BigDecimal("10000.00"));

            User userA = new User();
            userA.setId("USER_SEED_A");
            userA.setUsername("testuser1");
            userA.setPassword(passwordEncoder.encode("123456"));
            userA.setRole(UserRole.USER);
            userA.setAccountId(accountAId);
            userA.setCreatedAt(LocalDateTime.now());
            userRepository.save(userA);

            User userB = new User();
            userB.setId("USER_SEED_B");
            userB.setUsername("testuser2");
            userB.setPassword(passwordEncoder.encode("123456"));
            userB.setRole(UserRole.USER);
            userB.setAccountId(accountBId);
            userB.setCreatedAt(LocalDateTime.now());
            userRepository.save(userB);

            System.out.println("Tạo thành công Tài khoản nguồn:");
            System.out.println("- Username: testuser1");
            System.out.println("- Password: 123456");
            System.out.println("- Account ID: " + accountAId + " | Số dư: 50.000.000 VND");

            System.out.println("\nTạo thành công Tài khoản đích:");
            System.out.println("- Username: testuser2");
            System.out.println("- Password: 123456");
            System.out.println("- Account ID: " + accountBId + " | Số dư: 10.000 VND");
            System.out.println("====== KẾT THÚC TẠO DỮ LIỆU ======");
        }
    }
}
