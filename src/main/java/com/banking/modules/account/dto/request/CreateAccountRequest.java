package com.banking.modules.account.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {
    @PositiveOrZero(message = "Initial amount must be zero or positive")
    private BigDecimal amount;
}