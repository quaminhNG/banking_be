package com.banking.infrastructure.externalbank.providers;

import com.banking.infrastructure.externalbank.ExternalBankProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * Mẫu triển khai kết nối API với Ngân hàng MB (MBBank).
 * Người dùng cần tự điền logic kết nối API thực tế tại đây.
 */
@Component
@Slf4j
public class MBBankProvider implements ExternalBankProvider {

    @Value("${banking.provider.mb.base-url:https://api.mbbank.com.vn/}")
    private String baseUrl;

    @Value("${banking.provider.mb.api-key:}")
    private String apiKey;

    @Override
    public boolean validateAccount(String accountNumber) {
        log.info("MBBank: Đang kiểm tra tài khoản {}", accountNumber);
        
        // gọi API của MB Bank để kiểm tra số tài khoản có tồn tại hay không
        // Gợi ý: Sử dụng RestTemplate hoặc WebClient để gọi endpoint GET /v1/accounts/{accountNumber}
        
        return true; // Tạm thời trả về true
    }

    @Override
    public String executeTransfer(String toAccountNumber, BigDecimal amount, String currency, String idempotencyKey) {
        log.info("MBBank: Đang thực hiện chuyển khoản {} {} tới số tài khoản {}", amount, currency, toAccountNumber);
        
        // gọi API của MB Bank để thực hiện chuyển tiền liên ngân hàng
        // Gợi ý:
        // 1. Tạo request body theo định dạng API của MB Bank
        // 2. Thêm Header (Authorization, API-Key, Idempotency-Key)
        // 3. Thực hiện POST request tới endpoint chuyển tiền của MB Bank
        // 4. Parse kết quả trả về để lấy mã giao dịch (External Transaction ID)
        
        // Trả về một mã giao dịch giả lập để test flow
        return "MB-TX-" + java.util.UUID.randomUUID(); 
    }

    @Override
    public String getBankCode() {
        return "MB"; // Mã định danh ngân hàng trong hệ thống
    }
}
