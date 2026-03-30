# API Documentation - Banking Backend

Dưới đây là danh sách các endpoint hiện có trong hệ thống, bao gồm cấu trúc Input (Request) và Output (Response) để phục vụ việc xây dựng Documentation Site.

---

## 1. Account Module

### Tạo mới tài khoản (Create Account)
Khởi tạo một tài khoản mới với số dư ban đầu.

- **Endpoint:** `POST /api/v1/accounts`
- **Request Body (`CreateAccountRequest`):**
```json
{
  "amount": 1000000.00
}
```
- **Response Body (`CreateAccountResponse`):**
```json
{
  "message": "Account created successfully"
}
```

---

## 2. Transaction Module

### Nạp tiền (Deposit)
Thực hiện nạp tiền vào tài khoản thông qua hệ thống giao dịch có kiểm soát trùng lặp.

- **Endpoint:** `POST /api/v1/transaction/deposit`
- **Request Body (`TransactionRequest`):**
```json
{
  "accountId": "UUID_CỦA_ACCOUNT",
  "amount": 500000.00,
  "idempotencyKey": "MÃ_DUY_NHẤT_CHO_MỖI_REQUEST"
}
```
- **Response Body (`TransactionResponse`):**
```json
{
  "transactionId": "TX_UUID",
  "accountId": "UUID_CỦA_ACCOUNT",
  "amount": 500000.00,
  "type": "DEPOSIT",
  "status": "SUCCESS",
  "message": "Transaction successful.",
  "createdAt": "2026-03-30T16:00:00"
}
```

### Rút tiền (Withdraw)
Thực hiện rút tiền từ tài khoản với các bước kiểm tra tương tự nạp tiền.

- **Endpoint:** `POST /api/v1/transaction/withdraw`
- **Request Body (`TransactionRequest`):**
```json
{
  "accountId": "UUID_CỦA_ACCOUNT",
  "amount": 200000.00,
  "idempotencyKey": "MÃ_DUY_NHẤT_CHO_MỖI_REQUEST"
}
```
- **Response Body (`TransactionResponse`):**
```json
{
  "transactionId": "TX_UUID",
  "accountId": "UUID_CỦA_ACCOUNT",
  "amount": 200000.00,
  "type": "WITHDRAW",
  "status": "SUCCESS",
  "message": "Transaction successful.",
  "createdAt": "2026-03-30T16:05:00"
}
```

---

## Ghi chú về Idempotency
- Mỗi `TransactionRequest` phải đi kèm với một `idempotencyKey` duy nhất để tránh việc xử lý trùng lặp khi có lỗi mạng hoặc người dùng nhấn nút nhiều lần.
- Nếu gửi trùng `idempotencyKey` đã thành công, hệ thống sẽ trả về thông tin giao dịch cũ kèm thông báo: `"Duplicate request detected."`.
