package com.banking.modules.transfer.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private String currency;
    private String idempotencyKey;
}
