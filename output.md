# API Endpoints Testing Results

## 1. Register (Success)
**Endpoint:** `POST /api/v1/auth/register`

### Request
**Body:**
```json
{
  "username": "user_1775189981",
  "password": "Password@123"
}
```

### Response
**Status:** `200`
**Body:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyXzE3NzUxODk5ODEiLCJyb2xlIjoiVVNFUiIsImlhdCI6MTc3NTE4OTk4MSwiZXhwIjoxNzc1Mjc2MzgxfQ.VRf--iVDx0KjiNuz8IxB2b8VgocBsfvbEDjFrpcKxb9ecoGJVWMyDqELuzGe4FfCVnkNPeG3t-P22coS8LxfBA",
  "username": "user_1775189981",
  "role": "USER",
  "accountId": "707550b3-b6da-43d4-a665-8a6c221ec346"
}
```

## 2. Register (Fail - Duplicate Username)
**Endpoint:** `POST /api/v1/auth/register`

### Request
**Body:**
```json
{
  "username": "user_1775189981",
  "password": "Password@123"
}
```

### Response
**Status:** `400`
**Body:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Username already exists",
  "path": "/api/v1/auth/register",
  "timestamp": "2026-04-03T11:19:42.1289522"
}
```

## 3. Register (Fail - Validation Missing Fields)
**Endpoint:** `POST /api/v1/auth/register`

### Request
**Body:**
```json
{
  "username": ""
}
```

### Response
**Status:** `400`
**Body:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: username: Username must be between 3 and 50 characters, username: Username is required, password: Password is required",
  "path": "/api/v1/auth/register",
  "timestamp": "2026-04-03T11:19:42.1579186"
}
```

## 4. Login (Success)
**Endpoint:** `POST /api/v1/auth/login`

### Request
**Body:**
```json
{
  "username": "user_1775189981",
  "password": "Password@123"
}
```

### Response
**Status:** `200`
**Body:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyXzE3NzUxODk5ODEiLCJyb2xlIjoiVVNFUiIsImlhdCI6MTc3NTE4OTk4MiwiZXhwIjoxNzc1Mjc2MzgyfQ.hf1_RPNUP2i0xPzC6sRYvMg2NSH7CzsQNieJz3jlq-fUSEGgtym47Vxb57BuTnQSwn3lsf5RvoyLp5Fj6JsTiw",
  "username": "user_1775189981",
  "role": "USER",
  "accountId": "707550b3-b6da-43d4-a665-8a6c221ec346"
}
```

## 5. Login (Fail - Wrong Credentials)
**Endpoint:** `POST /api/v1/auth/login`

### Request
**Body:**
```json
{
  "username": "user_1775189981",
  "password": "WrongPassword@123"
}
```

### Response
**Status:** `400`
**Body:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid username or password",
  "path": "/api/v1/auth/login",
  "timestamp": "2026-04-03T11:19:42.3424772"
}
```

## 6. Create Account (Might need ADMIN)
**Endpoint:** `POST /api/v1/accounts`

### Request
**Headers:**
```json
{
  "Authorization": "Bearer <TOKEN>"
}
```
**Body:**
```json
{
  "amount": 500.0
}
```

### Response
**Status:** `403`
**Body:**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. You do not have permission to access this resource.",
  "path": "/api/v1/accounts",
  "timestamp": "2026-04-03T11:19:42.4107092"
}
```

## 7. Deposit (Success)
**Endpoint:** `POST /api/v1/transaction/deposit`

### Request
**Headers:**
```json
{
  "Authorization": "Bearer <TOKEN>"
}
```
**Body:**
```json
{
  "accountId": "707550b3-b6da-43d4-a665-8a6c221ec346",
  "amount": 1000.0,
  "idempotencyKey": "dep_1775189981"
}
```

### Response
**Status:** `200`
**Body:**
```json
{
  "transactionId": "dd2cc0eb-ad01-46be-b1b2-22ae839d127c",
  "accountId": "707550b3-b6da-43d4-a665-8a6c221ec346",
  "amount": 1000.0,
  "type": "DEPOSIT",
  "status": "SUCCESS",
  "message": "Transaction successful.",
  "createdAt": "2026-04-03T11:19:42.4554198"
}
```

## 8. Deposit (Fail - Negative Amount)
**Endpoint:** `POST /api/v1/transaction/deposit`

### Request
**Headers:**
```json
{
  "Authorization": "Bearer <TOKEN>"
}
```
**Body:**
```json
{
  "accountId": "707550b3-b6da-43d4-a665-8a6c221ec346",
  "amount": -100.0,
  "idempotencyKey": "dep_fail_1775189981"
}
```

### Response
**Status:** `400`
**Body:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: amount: Amount must be positive",
  "path": "/api/v1/transaction/deposit",
  "timestamp": "2026-04-03T11:19:42.5276508"
}
```

## 9. Withdraw
**Endpoint:** `POST /api/v1/transaction/withdraw`

### Request
**Headers:**
```json
{
  "Authorization": "Bearer <TOKEN>"
}
```
**Body:**
```json
{
  "accountId": "707550b3-b6da-43d4-a665-8a6c221ec346",
  "amount": 200.0,
  "idempotencyKey": "wit_1775189981"
}
```

### Response
**Status:** `200`
**Body:**
```json
{
  "transactionId": "f2e65061-41da-4382-9e5e-923213fffbc5",
  "accountId": "707550b3-b6da-43d4-a665-8a6c221ec346",
  "amount": 200.0,
  "type": "WITHDRAW",
  "status": "SUCCESS",
  "message": "Transaction successful.",
  "createdAt": "2026-04-03T11:19:42.5500164"
}
```

## 10. Withdraw (Fail - Insufficient Funds)
**Endpoint:** `POST /api/v1/transaction/withdraw`

### Request
**Headers:**
```json
{
  "Authorization": "Bearer <TOKEN>"
}
```
**Body:**
```json
{
  "accountId": "707550b3-b6da-43d4-a665-8a6c221ec346",
  "amount": 999999.0,
  "idempotencyKey": "wit_fail_1775189981"
}
```

### Response
**Status:** `400`
**Body:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient balance",
  "path": "/api/v1/transaction/withdraw",
  "timestamp": "2026-04-03T11:19:42.5971026"
}
```

## 11. Transfer (Local Bank)
**Endpoint:** `POST /api/v1/transaction/transfer`

### Request
**Headers:**
```json
{
  "Authorization": "Bearer <TOKEN>"
}
```
**Body:**
```json
{
  "fromAccountId": "707550b3-b6da-43d4-a665-8a6c221ec346",
  "toAccountId": "TARGET_ACC_123",
  "amount": 100.0,
  "currency": "VND",
  "idempotencyKey": "tra_1775189981",
  "toBankCode": ""
}
```

### Response
**Status:** `400`
**Body:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "The minimum transfer amount is 10000VND",
  "path": "/api/v1/transaction/transfer",
  "timestamp": "2026-04-03T11:19:42.6183674"
}
```

## 12. Transfer (External Bank VCB)
**Endpoint:** `POST /api/v1/transaction/transfer`

### Request
**Headers:**
```json
{
  "Authorization": "Bearer <TOKEN>"
}
```
**Body:**
```json
{
  "fromAccountId": "707550b3-b6da-43d4-a665-8a6c221ec346",
  "toAccountId": "EXT_ACC",
  "amount": 100.0,
  "currency": "VND",
  "idempotencyKey": "tra_ext_1775189981",
  "toBankCode": "VCB"
}
```

### Response
**Status:** `404`
**Body:**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Target external account not found in VCB",
  "path": "/api/v1/transaction/transfer",
  "timestamp": "2026-04-03T11:19:42.6330742"
}
```

## 13. Ledger Balance
**Endpoint:** `GET /api/v1/ledger/balance/707550b3-b6da-43d4-a665-8a6c221ec346`

### Request
**Headers:**
```json
{
  "Authorization": "Bearer <TOKEN>"
}
```

### Response
**Status:** `200`
**Body:**
```json
{
  "accountId": "707550b3-b6da-43d4-a665-8a6c221ec346",
  "balance": 800.0,
  "version": 2,
  "updatedAt": "2026-04-03T11:19:42.555125"
}
```

## 14. Ledger Balance (Fail - Unauthorized access)
**Endpoint:** `GET /api/v1/ledger/balance/OTHER_123`

### Request
**Headers:**
```json
{
  "Authorization": "Bearer <TOKEN>"
}
```

### Response
**Status:** `403`
**Body:**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied: not your account",
  "path": "/api/v1/ledger/balance/OTHER_123",
  "timestamp": "2026-04-03T11:19:42.6779402"
}
```

