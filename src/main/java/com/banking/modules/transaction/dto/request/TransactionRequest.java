package com.banking.modules.transaction.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    private String accountId;
    private BigDecimal amount;
    private String idempotencyKey;
}
