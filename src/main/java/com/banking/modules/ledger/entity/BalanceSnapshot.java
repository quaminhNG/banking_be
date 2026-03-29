package com.banking.modules.ledger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "balance_snapshots")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceSnapshot {
    @Id
    @Column(name = "account_id")
    private String accountId;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal balance;

    @Version
    private int version;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
