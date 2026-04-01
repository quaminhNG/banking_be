# External Bank Integration Guide (Vietnamese Banking)

Tài liệu này hướng dẫn cách nâng cấp các Adapter giả lập (Mock) thành kết nối API thực tế với các ngân hàng tại Việt Nam.

## 1. Cấu hình (Configuration)
Cập nhật các thông số trong `application.properties` với thông tin được ngân hàng cấp:
- `base-url`: Địa chỉ API Production của ngân hàng.
- `api-key`: Mã ứng dụng (Client ID).
- `api-secret`: Khóa bí mật (Client Secret).

## 2. Quy trình nâng cấp code (Implementation Steps)

### Bước A: Thực hiện gọi API thực tế
Sử dụng `RestTemplate` hoặc `WebClient` trong lớp Provider:
```java
@Autowired
private RestTemplate restTemplate;

public String executeTransfer(...) {
    // 1. Tạo JSON Request theo đặc tả của từng Bank (VCB khác TCB)
    // 2. Gọi API thông qua HTTP POST
    // 3. Xử lý kết quả trả về
}
```

### Bước B: Xử lý bảo mật (Security)
Hầu hết các ngân hàng VN (VietinBank, VCB) yêu cầu:
- **OAuth2**: Lấy Access Token trước khi thực hiện giao dịch.
- **Digital Signature (RSA)**: Ký số vào nội dung Request Body. 
  - Sử dụng `java.security.Signature` với Private Key của doanh nghiệp.
  - Chữ ký thường được gửi kèm trong HTTP Header (VD: `x-signature`).

## 3. Danh sách Header thông dụng
- `Authorization`: Bearer [Access Token]
- `X-IBM-Client-Id`: (Dành cho VietinBank iConnect)
- `Content-Type`: application/json

## 4. Lưu ý về IP Whitelisting
Ngân hàng thật thường yêu cầu bạn cung cấp **IP tĩnh** của Server. Họ sẽ chặn tất cả các yêu cầu không đến từ IP này.