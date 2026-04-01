# Banking API — Test Output

> Server: `http://localhost:8080`  
> DB: PostgreSQL `banking_db` (fresh, cleared trước khi test)  
> Ngày test: 2026-04-01

---

## Danh sách Endpoints

| # | Method | Path | Mô tả |
|---|--------|------|-------|
| 1 | POST | `/api/v1/accounts` | Tạo account |
| 2 | POST | `/api/v1/transaction/deposit` | Nạp tiền |
| 3 | POST | `/api/v1/transaction/withdraw` | Rút tiền |
| 4 | POST | `/api/v1/transaction/transfer` | Chuyển tiền |

---

## 1. POST `/api/v1/accounts` — Tạo Account

### Request Body
```json
{
  "amount": 500000   // optional — số dư khởi tạo, nếu bỏ qua thì mặc định 0
}
```

### 1a ✅ Tạo account với số dư ban đầu

**Request:**
```json
{ "amount": 500000 }
```

**Response `200 OK`:**
```json
{
  "accountId": "dcf7d9c0-e766-49a7-a053-5ae3b890e128",
  "message": "Account created successfully"
}
```

### 1b ✅ Tạo account không có số dư (balance = 0)

**Request:**
```json
{}
```

**Response `200 OK`:**
```json
{
  "accountId": "95315b8d-11d6-40cf-baf0-c9f9fbf5832c",
  "message": "Account created successfully"
}
```

> **Lưu ý:** `accountId` là UUID, dùng làm đầu vào cho các endpoint deposit/withdraw/transfer.

---

## 2. POST `/api/v1/transaction/deposit` — Nạp tiền

### Request Body
```json
{
  "accountId": "<uuid>",
  "amount": 50000,
  "idempotencyKey": "dep-001"   // optional nhưng nên có để tránh duplicate
}
```

### Business Rules
- Số tiền tối thiểu: **10,000 VND** (`TransactionConstants.MIN_DEPOSIT`)

---

### 2a ✅ Nạp tiền thành công

**Request:**
```json
{
  "accountId": "57d5f2d6-c28e-4ac7-b505-9e6651430f13",
  "amount": 50000,
  "idempotencyKey": "dep-001"
}
```

**Response `200 OK`:**
```json
{
  "transactionId": "28599b82-c88e-49e1-b8c9-1522d53a0434",
  "accountId": "57d5f2d6-c28e-4ac7-b505-9e6651430f13",
  "amount": 50000.0000,
  "type": "DEPOSIT",
  "status": "SUCCESS",
  "message": "Transaction successful.",
  "createdAt": "2026-04-01T12:17:47.691636"
}
```

---

### 2b ❌ Số tiền dưới mức tối thiểu (9,999 < 10,000)

**Request:**
```json
{
  "accountId": "57d5f2d6-c28e-4ac7-b505-9e6651430f13",
  "amount": 9999,
  "idempotencyKey": "dep-002"
}
```

**Response `400 Bad Request`:**
```json
{
  "status":    400,
  "error":     "Bad Request",
  "message":   "The minimum deposit amount is 10000VND",
  "path":      "/api/v1/transaction/deposit",
  "timestamp": "2026-04-01T13:25:00.000"
}
```

---

### 2c ✅ Idempotency — Gửi lại cùng `idempotencyKey`

**Request:** (giống hệt 2a, cùng key `dep-001`)
```json
{
  "accountId": "57d5f2d6-c28e-4ac7-b505-9e6651430f13",
  "amount": 50000,
  "idempotencyKey": "dep-001"
}
```

**Response `200 OK`:**
```json
{
  "transactionId": "28599b82-c88e-49e1-b8c9-1522d53a0434",
  "accountId": "57d5f2d6-c28e-4ac7-b505-9e6651430f13",
  "amount": 50000.0000,
  "type": "DEPOSIT",
  "status": "SUCCESS",
  "message": "Duplicate request detected.",
  "createdAt": "2026-04-01T12:17:47.691636"
}
```

> ✅ Trả về **cùng transactionId** như lần đầu — không tạo transaction mới, không nạp tiền thêm.

---

### 2d ❌ Account không tồn tại

**Request:**
```json
{
  "accountId": "non-existent-id",
  "amount": 50000,
  "idempotencyKey": "dep-003"
}
```

**Response `400 Bad Request`:**
```json
{
  "status":    400,
  "error":     "Bad Request",
  "message":   "Account not found",
  "path":      "/api/v1/transaction/deposit",
  "timestamp": "2026-04-01T13:25:00.000"
}
```

---

## 3. POST `/api/v1/transaction/withdraw` — Rút tiền

### Request Body
```json
{
  "accountId": "<uuid>",
  "amount": 20000,
  "idempotencyKey": "wd-001"
}
```

### Business Rules
- Số tiền tối thiểu: **10,000 VND**
- Số tiền tối đa: **10,000,000 VND**
- Số dư phải đủ

---

### 3a ✅ Rút tiền thành công

**Request:**
```json
{
  "accountId": "57d5f2d6-c28e-4ac7-b505-9e6651430f13",
  "amount": 20000,
  "idempotencyKey": "wd-001"
}
```

**Response `200 OK`:**
```json
{
  "transactionId": "48df8e37-5bdd-4d2b-86cc-49d4f3871ca8",
  "accountId": "57d5f2d6-c28e-4ac7-b505-9e6651430f13",
  "amount": 20000.0000,
  "type": "WITHDRAW",
  "status": "SUCCESS",
  "message": "Transaction successful.",
  "createdAt": "2026-04-01T12:17:48.265"
}
```

---

### 3b ❌ Số tiền dưới mức tối thiểu (5,000 < 10,000)

**Request:**
```json
{
  "accountId": "57d5f2d6-c28e-4ac7-b505-9e6651430f13",
  "amount": 5000,
  "idempotencyKey": "wd-002"
}
```

**Response `400 Bad Request`:**
```json
{
  "status":    400,
  "error":     "Bad Request",
  "message":   "The minimum withdrawal amount is 10000VND",
  "path":      "/api/v1/transaction/withdraw",
  "timestamp": "2026-04-01T13:25:00.000"
}
```

---

### 3c ❌ Số tiền vượt mức tối đa (99,999,999 > 10,000,000)

**Request:**
```json
{
  "accountId": "57d5f2d6-c28e-4ac7-b505-9e6651430f13",
  "amount": 99999999,
  "idempotencyKey": "wd-003"
}
```

**Response `400 Bad Request`:**
```json
{
  "status":    400,
  "error":     "Bad Request",
  "message":   "The maximum withdrawal amount is 10000000VND",
  "path":      "/api/v1/transaction/withdraw",
  "timestamp": "2026-04-01T13:25:00.000"
}
```

---

### 3d ❌ Số dư không đủ

**Request:** (acc2 có số dư 0)
```json
{
  "accountId": "4de82202-d6d7-4437-8f88-6e048612f38d",
  "amount": 10000,
  "idempotencyKey": "wd-004"
}
```

**Response `400 Bad Request`:**
```json
{
  "status":    400,
  "error":     "Bad Request",
  "message":   "Insufficient balance",
  "path":      "/api/v1/transaction/withdraw",
  "timestamp": "2026-04-01T13:25:00.000"
}
```

---

## 4. POST `/api/v1/transaction/transfer` — Chuyển tiền

### Request Body
```json
{
  "fromAccountId": "<uuid>",
  "toAccountId":   "<uuid>",
  "amount":        30000,
  "currency":      "VND",
  "idempotencyKey": "tr-001"
}
```

### Business Rules
- Số tiền tối thiểu: **10,000 VND** (`TransferConstants.MIN_TRANSFER`)
- Không được chuyển cho chính mình
- Số dư account nguồn phải đủ
- Cần phải tạo cả 2 account trước

---

### 4a ✅ Chuyển tiền thành công

**Request:**
```json
{
  "fromAccountId": "57d5f2d6-c28e-4ac7-b505-9e6651430f13",
  "toAccountId":   "4de82202-d6d7-4437-8f88-6e048612f38d",
  "amount":        30000,
  "currency":      "VND",
  "idempotencyKey": "tr-001"
}
```

**Response `200 OK`:**
```json
{
  "transactionId": "63b02256-b3e7-4361-94b7-e8f263017481",
  "fromAccountId": "57d5f2d6-c28e-4ac7-b505-9e6651430f13",
  "toAccountId":   "4de82202-d6d7-4437-8f88-6e048612f38d",
  "amount":        30000.0000,
  "currency":      "VND",
  "status":        "SUCCESS",
  "message":       "Transfer successful.",
  "createdAt":     "2026-04-01T12:17:48.582"
}
```

---

### 4b ❌ Số tiền dưới mức tối thiểu (5,000 < 10,000)

**Request:**
```json
{
  "fromAccountId": "57d5f2d6-c28e-4ac7-b505-9e6651430f13",
  "toAccountId":   "4de82202-d6d7-4437-8f88-6e048612f38d",
  "amount":        5000,
  "currency":      "VND",
  "idempotencyKey": "tr-002"
}
```

**Response `400 Bad Request`:**
```json
{
  "status":    400,
  "error":     "Bad Request",
  "message":   "The minimum transfer amount is 10000VND",
  "path":      "/api/v1/transaction/transfer",
  "timestamp": "2026-04-01T12:25:43.116"
}
```

> ✅ Transfer ném exception trực tiếp (không qua wrapper) → HTTP 400 đúng chuẩn REST.

---

### 4c ❌ Chuyển tiền cho chính mình

**Request:**
```json
{
  "fromAccountId": "57d5f2d6-c28e-4ac7-b505-9e6651430f13",
  "toAccountId":   "57d5f2d6-c28e-4ac7-b505-9e6651430f13",
  "amount":        10000,
  "currency":      "VND",
  "idempotencyKey": "tr-003"
}
```

**Response `400 Bad Request`:**
```json
{
  "status":    400,
  "error":     "Bad Request",
  "message":   "Can't transfer to yourself!",
  "path":      "/api/v1/transaction/transfer",
  "timestamp": "2026-04-01T12:25:43.272"
}
```

---

### 4d ❌ Số dư không đủ để chuyển

**Request:**
```json
{
  "fromAccountId": "57d5f2d6-c28e-4ac7-b505-9e6651430f13",
  "toAccountId":   "4de82202-d6d7-4437-8f88-6e048612f38d",
  "amount":        9999999,
  "currency":      "VND",
  "idempotencyKey": "tr-004"
}
```

**Response `400 Bad Request`:**
```json
{
  "status":    400,
  "error":     "Bad Request",
  "message":   "Insufficient balance",
  "path":      "/api/v1/transaction/transfer",
  "timestamp": "2026-04-01T12:25:43.582"
}
```

---

### 4e ✅ Idempotency — Gửi lại cùng `idempotencyKey`

**Request:** (giống hệt 4a, cùng key `tr-001`)
```json
{
  "fromAccountId": "57d5f2d6-c28e-4ac7-b505-9e6651430f13",
  "toAccountId":   "4de82202-d6d7-4437-8f88-6e048612f38d",
  "amount":        30000,
  "currency":      "VND",
  "idempotencyKey": "tr-001"
}
```

**Response `200 OK`:**
```json
{
  "transactionId": "63b02256-b3e7-4361-94b7-e8f263017481",
  "fromAccountId": "57d5f2d6-c28e-4ac7-b505-9e6651430f13",
  "toAccountId":   "4de82202-d6d7-4437-8f88-6e048612f38d",
  "amount":        30000.0000,
  "currency":      "VND",
  "status":        "SUCCESS",
  "message":       "Transfer already completed.",
  "createdAt":     "2026-04-01T12:17:48.582"
}
```

> ✅ Trả về **cùng transactionId** — không chuyển tiền lần 2. Message đổi thành `"Transfer already completed."`.

---

## Tổng kết

| ID | Endpoint | Case | HTTP | Kết quả |
|----|----------|------|------|--------|
| 1a | POST /accounts | Tạo với balance | 200 | ✅ accountId trả về |
| 1b | POST /accounts | Tạo không balance | 200 | ✅ balance = 0 |
| 2a | POST /deposit | Thành công | 200 | ✅ status: SUCCESS |
| 2b | POST /deposit | Dưới min 10k | 400 | ✅ Bad Request |
| 2c | POST /deposit | Idempotency dup | 200 | ✅ Cùng txId, không nạp lại |
| 2d | POST /deposit | Account not found | 400 | ✅ Bad Request |
| 3a | POST /withdraw | Thành công | 200 | ✅ status: SUCCESS |
| 3b | POST /withdraw | Dưới min 10k | 400 | ✅ Bad Request |
| 3c | POST /withdraw | Vượt max 10M | 400 | ✅ Bad Request |
| 3d | POST /withdraw | Số dư không đủ | 400 | ✅ Bad Request |
| 4a | POST /transfer | Thành công | 200 | ✅ status: SUCCESS |
| 4b | POST /transfer | Dưới min 10k | 400 | ✅ Bad Request |
| 4c | POST /transfer | Chuyển cho mình | 400 | ✅ Bad Request |
| 4d | POST /transfer | Số dư không đủ | 400 | ✅ Bad Request |
| 4e | POST /transfer | Idempotency dup | 200 | ✅ Cùng txId, không chuyển lại |

