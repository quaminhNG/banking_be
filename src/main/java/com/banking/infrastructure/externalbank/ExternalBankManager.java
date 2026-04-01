package com.banking.infrastructure.externalbank;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Trạm trung chuyển để tìm kiếm và gọi đúng Ngân hàng đối tác.
 */
@Service
public class ExternalBankManager {

    private final Map<String, ExternalBankProvider> providers = new ConcurrentHashMap<>(); // String,
                                                                                           // ExternalBankProvider:
                                                                                           // chuỗi và các tên dịch vụ
                                                                                           // được nạp vào từ class

    public ExternalBankManager(List<ExternalBankProvider> bankProviders) {
        for (ExternalBankProvider provider : bankProviders) { // duyệt tên và cách thức hoạt động của các ngân hàng
            providers.put(provider.getBankCode(), provider);
        }
    }

    /**
     * Tìm ngân hàng theo mã.
     */
    public ExternalBankProvider getProvider(String bankCode) {
        ExternalBankProvider provider = providers.get(bankCode);
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported external bank: " + bankCode);
        }
        return provider;
    }

    /**
     * Lấy danh sách các ngân hàng đang được hỗ trợ kết nối.
     */
    public List<String> getSupportedBanks() {
        return List.copyOf(providers.keySet()); // copyOf: tạo ra một bản sao của danh sách các ngân hàng và chỉ được
                                                // xem k được hành động gì
    }
}
