package com.banking.infrastructure.externalbank;

import java.math.BigDecimal;

/**
 * Giao diện chung cho mọi ngân hàng bên thứ 3 (Sandbox/Real).
 * Giúp hệ thống không phụ thuộc vào từng API riêng biệt của từng Bank.
 */
public interface ExternalBankProvider {

    /**
     * Kiểm tra tài khoản đích có tồn tại ở ngân hàng đối tác không.
     */
    boolean validateAccount(String accountNumber);

    /**
     * Thực hiện yêu cầu chuyển khoản sang ngân hàng đối tác.
     * @return Mã giao dịch (External Transaction ID) từ ngân hàng đó trả về.
     */
    String executeTransfer(String toAccountNumber, BigDecimal amount, String currency, String idempotencyKey);

    /**
     * Mã định danh của ngân hàng này (VD: "MOCK_BANK", "VIETIN", "VCB").
     */
    String getBankCode();
}
