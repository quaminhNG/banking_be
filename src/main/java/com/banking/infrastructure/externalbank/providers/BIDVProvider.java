package com.banking.infrastructure.externalbank.providers;

import com.banking.infrastructure.externalbank.ExternalBankProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@Slf4j
public class BIDVProvider implements ExternalBankProvider {

    @Value("${banking.provider.bidv.base-url}")
    private String baseUrl;

    @Override
    public boolean validateAccount(String accountNumber) {
        log.info("BIDV: Validating account {}", accountNumber);
        return accountNumber != null && accountNumber.startsWith("120");
    }

    @Override
    public String executeTransfer(String toAccountNumber, BigDecimal amount, String currency, String idempotencyKey) {
        log.info("BIDV: Transfer {} {} to account {}", amount, currency, toAccountNumber);
        return "BIDV-" + java.util.UUID.randomUUID();
    }

    @Override
    public String getBankCode() {
        return "BIDV";
    }
}
