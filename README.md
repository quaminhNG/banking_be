# 🏦 Banking Backend API

<div align="center">

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.x-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)

**RESTful Banking API với Concurrency-Safe Transfer, Idempotency, JWT Auth, Rate Limiting**

[📖 Swagger UI](http://localhost:8080/swagger-ui.html) · [📬 Postman Collection](#-postman-collection) · [🏗️ Architecture](#️-kiến-trúc-hệ-thống)

</div>

---

## ✨ Tính năng chính

| Tính năng | Công nghệ | Mô tả |
|-----------|-----------|-------|
| 🔐 **JWT Authentication** | Spring Security + JJWT 0.12 | Stateless auth, token 24h |
| 🛡️ **Role-Based Access Control** | `@PreAuthorize`, `@EnableMethodSecurity` | USER / ADMIN roles |
| ⚡ **Rate Limiting** | Bucket4j + Caffeine Cache | 20 req/phút mỗi user |
| 🔁 **Idempotency Key** | Custom Filter + DB Check | Tránh giao dịch trùng lặp |
| 🔒 **Pessimistic Locking** | JPA `PESSIMISTIC_WRITE` | An toàn khi 1000+ concurrent requests |
| 📊 **Ledger System** | Double-Entry Bookkeeping | Ghi nhật ký kế toán kép |
| 🐰 **Async Logging** | RabbitMQ | Transaction log bất đồng bộ |
| 🏦 **External Transfer** | Strategy Pattern + HTTP Client | Hỗ trợ kết nối MB Bank (Template) |

---

## 🚀 Chạy nhanh (Quick Start)

### Yêu cầu
- Java 21+
- Docker & Docker Compose

### 1. Clone & Start

```bash
git clone https://github.com/quaminhNG/banking_be.git
cd banking_be
docker-compose up -d   # Khởi động PostgreSQL
./mvnw spring-boot:run
```

### 2. Truy cập Swagger UI

```
http://localhost:8080/swagger-ui.html
```

### 3. Tài khoản mẫu (tự động tạo khi khởi động lần đầu)

| Username | Password | Role | Account ID | Số dư ban đầu |
|----------|----------|------|------------|---------------|
| `testuser1` | `123456` | USER | `ACC_SEED_A` | 50,000,000 VND |
| `testuser2` | `123456` | USER | `ACC_SEED_B` | 10,000 VND |

### 4. Test API

```bash
# Bước 1: Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser1","password":"123456"}' | jq -r '.token')

# Bước 2: Xem số dư
curl http://localhost:8080/api/v1/ledger/balance/ACC_SEED_A \
  -H "Authorization: Bearer $TOKEN"

# Bước 3: Chuyển tiền
curl -X POST http://localhost:8080/api/v1/transaction/transfer \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": "ACC_SEED_A",
    "toAccountId": "ACC_SEED_B",
    "amount": 500000,
    "currency": "VND",
    "idempotencyKey": "demo-key-001"
  }'
```

---

## 📬 Postman Collection

Import file [`Banking-API.postman_collection.json`](./Banking-API.postman_collection.json) vào Postman để test tất cả API với test scripts tự động.

**Collection bao gồm:**
- ✅ Auto-save JWT token sau khi login
- ✅ Test scripts kiểm tra response tự động
- ✅ Demo các edge cases: 401, 403, 400, Idempotency
- ✅ Demo Rate Limiting (429)

---

## 🏗️ Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────────┐
│                    Client (HTTP)                         │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│                  Filter Chain                            │
│   RateLimitFilter (Bucket4j) ──► JwtFilter (JJWT)       │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│                   Controllers                            │
│  AuthController │ TransferController │ LedgerController  │
│  DepositController │ WithdrawController │ AccountController│
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│                    Services                              │
│                                                         │
│  TransferService ──────────────────────────────────────┐│
│  (Pessimistic Lock + Idempotency)                      ││
│                                                         ││
│  ExternalTransferService (Strategy Pattern)             ││
│  ├── MBBankProvider (Template)                          ││
│  └── ... (Thêm Bank khác tại đây)                      ││
│                                                         ││
│  LedgerService (Double-Entry Bookkeeping)               ││
│  TransactionService (Deposit / Withdraw)                ││
└──────────────────────┬──────────────────────────────────┘│
                       │                                   │
┌──────────────────────▼───────────────┐  ┌───────────────▼┐
│         PostgreSQL Database          │  │   RabbitMQ      │
│  users │ accounts │ transactions     │  │  (Async Log)    │
│  ledger_entries │ balance_snapshots  │  │                 │
└──────────────────────────────────────┘  └────────────────┘
```

---

## 🔒 Bảo mật

### JWT Flow
```
Client  ──POST /auth/login──►  Server
        ◄── JWT Token (24h) ──
        
Client  ──GET /api/... ──────►  JwtFilter ──► SecurityContext
        (Authorization: Bearer <token>)       (Authenticated)
```

### Security Rules
```java
.requestMatchers("/api/v1/auth/**").permitAll()           // Public
.requestMatchers(POST, "/api/v1/accounts").hasRole("ADMIN") // ADMIN only
.anyRequest().authenticated()                              // JWT required
```

### Rate Limiting (Bucket4j)
- **20 requests / phút** mỗi user
- Trả về `429 Too Many Requests` khi vượt ngưỡng
- Sử dụng Caffeine Cache in-memory

---

## 🔁 Idempotency

```
Request 1: POST /transfer  { idempotencyKey: "KEY-001", amount: 500000 }
           → Tạo giao dịch mới: TXN_abc123 ✅

Request 2: POST /transfer  { idempotencyKey: "KEY-001", amount: 500000 }
           → Trả về TXN_abc123 (cũ), KHÔNG tạo giao dịch mới ✅
```

---

## ⚡ Concurrency Test

Test 1000 giao dịch đồng thời với 20 threads:

```bash
./mvnw test -Dtest=TransferConcurrencyTest
```

**Kết quả đảm bảo:**
- ✅ Tổng số tiền trong hệ thống không thay đổi
- ✅ 1000/1000 giao dịch thành công
- ✅ Không có race condition, không mất tiền

```
============== KẾT QUẢ FINAL ==============
Thành công: 1000 | Thất bại: 0
Tổng tiền ban đầu: 20000000.00 | Tổng tiền hiện tại: 20000000.00
```

---

## 📁 Cấu trúc Module

```
src/main/java/com/banking/
├── config/                    # Cấu hình (OpenAPI, DataSeeder, RabbitMQ)
├── security/                  # JWT Filter, Security Config, Rate Limit Filter  
├── common/                    # Shared DTOs, Exception Handlers
├── middleware/                # Idempotency Filter
└── modules/
    ├── auth/                  # Đăng ký, Đăng nhập
    ├── account/               # Quản lý tài khoản
    ├── ledger/                # Hệ thống Ledger, Balance Snapshot
    ├── transaction/           # Deposit, Withdraw
    ├── transfer/              # Internal & External Transfer
    │   ├── service/
    │   │   ├── TransferService.java        # Pessmistic Lock
    │   │   └── ExternalTransferService.java # Strategy Pattern
    │   ├── domain/            # Bank Provider Interface
    │   └── consumer/          # RabbitMQ Consumer
    └── audit/                 # Audit Trail
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.4.0 |
| Language | Java 21 |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA + Hibernate |
| Security | Spring Security + JJWT 0.12.6 |
| Message Queue | RabbitMQ (Spring AMQP) |
| Rate Limiting | Bucket4j 8.10.1 + Caffeine |
| API Docs | SpringDoc OpenAPI 2.7.0 (Swagger UI) |
| Build | Maven |
| Container | Docker + Docker Compose |
| Testing | JUnit 5 + Mockito + Awaitility |

---

## 📋 API Endpoints tóm tắt

| Method | Endpoint | Auth | Mô tả |
|--------|----------|------|-------|
| POST | `/api/v1/auth/register` | ❌ | Đăng ký tài khoản |
| POST | `/api/v1/auth/login` | ❌ | Đăng nhập, nhận JWT |
| POST | `/api/v1/accounts` | ✅ ADMIN | Tạo tài khoản ngân hàng |
| GET | `/api/v1/ledger/balance/{accountId}` | ✅ USER | Xem số dư |
| POST | `/api/v1/transaction/deposit` | ✅ USER | Nạp tiền |
| POST | `/api/v1/transaction/withdraw` | ✅ USER | Rút tiền |
| POST | `/api/v1/transaction/transfer` | ✅ USER | Chuyển tiền (nội bộ / liên NH) |

---

<div align="center">
Made with ❤️ by <a href="https://github.com/quaminhNG">Nguyen Minh Qua</a>
</div>
