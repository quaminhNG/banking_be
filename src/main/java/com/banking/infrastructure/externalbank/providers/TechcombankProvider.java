package com.banking.infrastructure.externalbank.providers;

import com.banking.infrastructure.externalbank.ExternalBankProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@Slf4j
public class TechcombankProvider implements ExternalBankProvider {

    @Value("${banking.provider.tcb.base-url}")
    private String baseUrl;

    @Value("${banking.provider.tcb.api-key}")
    private String apiKey;

    @Override
    public boolean validateAccount(String accountNumber) {
        log.info("TCB: Validating account {}", accountNumber);
        return accountNumber != null && accountNumber.startsWith("190");
    }

    @Override
    public String executeTransfer(String toAccountNumber, BigDecimal amount, String currency, String idempotencyKey) {
        log.info("TCB: Transfer {} {} to account {}", amount, currency, toAccountNumber);
        return "TCB-" + java.util.UUID.randomUUID();
    }

    @Override
    public String getBankCode() {
        return "TCB";
    }
}
