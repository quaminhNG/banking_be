package com.banking.modules.ledger.service;

import com.banking.modules.ledger.entity.LedgerEntry;
import com.banking.modules.ledger.entity.LedgerType;
import com.banking.modules.ledger.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {
    private final LedgerRepository ledgerRepository;

    @Transactional
    public void createInitialEntry(String accountId, BigDecimal amount) {
        LedgerEntry entry = new LedgerEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setAccountId(accountId);
        entry.setType(LedgerType.CREDIT);
        entry.setAmount(amount != null ? amount : BigDecimal.ZERO);
        entry.setCurrency("VND");
        entry.setCreatedAt(LocalDateTime.now());
        ledgerRepository.save(entry);
    }
}
