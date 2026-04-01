package com.banking.infrastructure.externalbank.providers;

import com.banking.infrastructure.externalbank.ExternalBankProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@Slf4j
public class ACBProvider implements ExternalBankProvider {

    @Value("${banking.provider.acb.base-url}")
    private String baseUrl;

    @Override
    public boolean validateAccount(String accountNumber) {
        log.info("ACB: Validating account {}", accountNumber);
        return accountNumber != null && accountNumber.startsWith("200");
    }

    @Override
    public String executeTransfer(String toAccountNumber, BigDecimal amount, String currency, String idempotencyKey) {
        log.info("ACB: Transfer {} {} to account {}", amount, currency, toAccountNumber);
        return "ACB-" + java.util.UUID.randomUUID();
    }

    @Override
    public String getBankCode() {
        return "ACB";
    }
}
