# 🏦 Banking Backend API

<div align="center">

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Kafka](https://img.shields.io/badge/Kafka-Apache%20Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)

**RESTful Banking API với Concurrency-Safe Transfer, Idempotency, JWT Auth, Rate Limiting**
</div>

## 🚀 API Endpoints (Live Demo)

Server đã được deploy!!

**Lưu ý:** Đối với các API yêu cầu xác thực, bạn cần thêm Header `Authorization: Bearer <jwt_token>` (Token lấy từ API Login/Register).

### 1. System
- **Health Check:** `GET http://51.20.79.190:8080/api/v1/health`
  - Mục đích: Kiểm tra trạng thái hoạt động của server.

### 2. 🔐 Authentication
- **Đăng ký (Register):** `POST http://51.20.79.190:8080/api/v1/auth/register`
  - **Body (JSON):**
    ```json
    {
      "username": "newuser",
      "password": "password123"
    }
    ```
- **Đăng nhập (Login):** `POST http://51.20.79.190:8080/api/v1/auth/login`
  - **Body (JSON):**
    ```json
    {
      "username": "testuser1",
      "password": "123456"
    }
    ```
    *(Tài khoản có sẵn: `testuser1`/`123456` với AccountId: `ACC_SEED_A`, `testuser2`/`123456` với AccountId: `ACC_SEED_B`)*

### 3. 💰 Transaction (Nạp & Rút Tiền)
- **Nạp tiền (Deposit):** `POST http://51.20.79.190:8080/api/v1/transaction/deposit`
  - **Headers:** `Authorization: Bearer <token>`
  - **Body (JSON):**
    ```json
    {
      "accountId": "ACC_SEED_A",
      "amount": 1000000,
      "idempotencyKey": "DEPOSIT-12345"
    }
    ```
- **Rút tiền (Withdraw):** `POST http://51.20.79.190:8080/api/v1/transaction/withdraw`
  - **Headers:** `Authorization: Bearer <token>`
  - **Body (JSON):**
    ```json
    {
      "accountId": "ACC_SEED_A",
      "amount": 500000,
      "idempotencyKey": "WITHDRAW-12345"
    }
    ```

### 4. 🔄 Transfer (Chuyển Khoản)
- **Chuyển khoản Nội bộ / Liên Ngân Hàng:** `POST http://51.20.79.190:8080/api/v1/transaction/transfer`
  - **Headers:** `Authorization: Bearer <token>`
  - **Body (JSON) Chuyển Nội bộ:**
    ```json
    {
      "fromAccountId": "ACC_SEED_A",
      "toAccountId": "ACC_SEED_B",
      "amount": 500000,
      "currency": "VND",
      "idempotencyKey": "TRANSFER-12345"
    }
    ```
  - **Body (JSON) Liên Ngân Hàng (VD: VCB):**
    ```json
    {
      "fromAccountId": "ACC_SEED_A",
      "toAccountId": "9901234567890",
      "amount": 100000,
      "currency": "VND",
      "toBankCode": "VCB",
      "idempotencyKey": "EXT-TRANSFER-12345"
    }
    ```

### 5. 📊 Ledger (Xem Số Dư)
- **Xem Số Dư:** `GET http://51.20.79.190:8080/api/v1/ledger/balance/{accountId}`
  - **Headers:** `Authorization: Bearer <token>`
  - **Ví dụ URL:** `http://51.20.79.190:8080/api/v1/ledger/balance/ACC_SEED_A`

### 6. 🏦 Account (Quản lý Tài Khoản)
- **Tạo Tài Khoản (Chỉ ADMIN):** `POST http://51.20.79.190:8080/api/v1/accounts`
  - **Headers:** `Authorization: Bearer <token>`
  - **Body (JSON):**
    ```json
    {
      "accountId": "ACC_NEW_TEST"
    }
    ```

### 7. 🔔 Notifications (Thông Báo)
- **Đăng ký nhận Email:** `POST http://51.20.79.190:8080/api/v1/notifications/subscribe`
  - **Headers:** `Authorization: Bearer <token>`
  - **Body (JSON):**
    ```json
    {
      "email": "your-email@gmail.com"
    }
    ```
