package com.banking.common.constants;

import java.math.BigDecimal;

public class TransactionConstants {
    public static final BigDecimal MIN_DEPOSIT = new BigDecimal("10000");
    public static final BigDecimal MIN_WITHDRAW = new BigDecimal("10000");
    public static final BigDecimal MAX_WITHDRAW = new BigDecimal("10000000");
    public static final String CURRENCY_VND = "VND";
}
