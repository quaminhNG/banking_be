package com.banking.infrastructure.externalbank;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mock
 */
@Component
@Slf4j
public class MockExternalBank implements ExternalBankProvider {

    @Override
    public boolean validateAccount(String accountNumber) {
        log.info("[MOCK_BANK] Validating account: {}", accountNumber);
        return accountNumber != null && accountNumber.startsWith("999") && accountNumber.length() >= 5;
    }

    @Override
    public String executeTransfer(String toAccountNumber, BigDecimal amount, String currency, String idempotencyKey) {
        log.info("[MOCK_BANK] Executing transfer of {} {} to account {}", amount, currency, toAccountNumber);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return "MOCK-TX-" + UUID.randomUUID().toString();
    }

    @Override
    public String getBankCode() {
        return "MOCK_BANK";
    }
}
