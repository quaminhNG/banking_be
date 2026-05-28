package com.banking.modules.transfer.event;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransferCompletedEvent implements Serializable {
    private String transactionId;
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private String currency;
}
