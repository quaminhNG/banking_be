package com.banking.modules.ledger.repository;

import com.banking.modules.ledger.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LedgerRepository extends JpaRepository<LedgerEntry, String> {
    List<LedgerEntry> findByAccountId(String accountId);
}
