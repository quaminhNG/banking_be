package com.banking.modules.ledger.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class BalanceSnapshotRequest {
    private String accountId;
    private BigDecimal amount;
}
