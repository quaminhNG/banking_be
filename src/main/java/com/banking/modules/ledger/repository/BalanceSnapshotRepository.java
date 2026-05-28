package com.banking.modules.ledger.repository;

import com.banking.modules.ledger.entity.BalanceSnapshot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BalanceSnapshotRepository extends JpaRepository<BalanceSnapshot, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BalanceSnapshot b WHERE b.accountId = :id")
    Optional<BalanceSnapshot> findByIdForUpdate(@Param("id") String id);
}
