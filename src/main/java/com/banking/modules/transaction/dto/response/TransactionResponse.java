package com.banking.modules.transaction.dto.response;

import com.banking.modules.transaction.entity.TransactionStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private String type;
    private TransactionStatus status;
    private String message;
    private LocalDateTime createdAt;
}
