BANKING BACKEND SYSTEM - PRODUCTION GRADE (LEDGER-FIRST)

==================================================
🌐 GLOBAL PRINCIPLES (STRICT)
=============================

1. SOURCE OF TRUTH = LEDGER

* Không bao giờ tin vào balance table
* Balance chỉ là derived data (cache)

2. IMMUTABILITY

* Không update transaction/ledger đã ghi
* Chỉ append (append-only system)

3. IDEMPOTENCY

* Mọi request thay đổi tiền phải idempotent
* Idempotency scoped theo (user + endpoint)

4. CONSISTENCY FIRST

* Ưu tiên Strong Consistency hơn Performance

5. CONCURRENCY SAFE

* Phải xử lý race condition ở DB level

6. AUDITABLE

* Mọi thay đổi phải trace được (who, when, before, after)

7. VERSIONED API

* /api/v1/...

Architecture:
Client → Controller → Service → Repository → Database

==================================================
🏗️ CORE DATA MODEL (LEDGER-BASED)
==================================

TABLE: accounts

* id (UUID, PK)
* status (ACTIVE | LOCKED | CLOSED)
* created_at

TABLE: ledger_entries (SOURCE OF TRUTH)

* id (UUID, PK)
* account_id (FK, INDEX)
* type (DEBIT | CREDIT)
* amount (DECIMAL(19,4))
* currency (VARCHAR(3))
* reference_id (UUID) (transaction_id / transfer_id)
* created_at

TABLE: transactions

* id (UUID, PK)
* idempotency_key (VARCHAR, NOT NULL)
* endpoint (VARCHAR)
* request_hash (VARCHAR)
* status (PENDING | SUCCESS | FAILED)
* created_at
* UNIQUE(idempotency_key, endpoint)

TABLE: balance_snapshots (CACHE)

* account_id (PK)
* balance (DECIMAL(19,4))
* version (INT)
* updated_at

TABLE: audit_logs

* id (UUID)
* request_id
* user_id
* action
* metadata (JSON)
* created_at

==================================================
🏦 STAGE 1: ACCOUNT + LEDGER INIT
=================================

FEATURES:

* Create account
* Initialize ledger (balance = 0)

BUSINESS RULES:

RULE-001: Account phải ACTIVE để sử dụng
RULE-002: Ledger entry đầu tiên = 0 (optional init)

API:

POST /api/v1/accounts

FLOW:

1. Validate input
2. Create account
3. Create initial ledger entry (0)
4. Log audit

==================================================
💰 STAGE 2: DEPOSIT / WITHDRAW (LEDGER-BASED)
=============================================

FEATURES:

* Deposit
* Withdraw

BUSINESS RULES:

RULE-101: Balance = SUM(ledger_entries)
RULE-102: Không cho withdraw nếu balance < amount
RULE-103: Mỗi operation = 1 transaction record
RULE-104: Idempotency required

API:

POST /api/v1/deposit
Headers:

* Idempotency-Key

Request:
{
"accountId": "...",
"amount": 100.00
}

FLOW (DEPOSIT):

1. Validate request
2. Check idempotency
3. Start DB Transaction
4. Insert CREDIT ledger entry
5. Update balance_snapshot (optional)
6. Save transaction
7. Commit

FLOW (WITHDRAW):

1. Validate request
2. Check idempotency
3. Start DB Transaction
4. Lock account snapshot (SELECT ... FOR UPDATE)
5. Calculate balance
6. If insufficient → fail
7. Insert DEBIT ledger entry
8. Update snapshot
9. Commit

==================================================
🔄 STAGE 3: TRANSFER (DOUBLE ENTRY)
===================================

FEATURES:

* Transfer A → B

BUSINESS RULES:

RULE-201: Double-entry (A DEBIT, B CREDIT)
RULE-202: Atomic transaction
RULE-203: Lock ordering (prevent deadlock)
RULE-204: Same currency required

API:

POST /api/v1/transfer
Headers:

* Idempotency-Key

FLOW:

1. Validate input

2. Check idempotency

3. Start DB Transaction

4. Lock accounts (ORDER BY account_id ASC)

5. Calculate balances

6. Validate sufficient balance

7. Insert 2 ledger entries:

   * A: DEBIT
   * B: CREDIT

8. Update balance_snapshot

9. Save transaction

10. Commit

==================================================
⚙️ CONCURRENCY & ISOLATION
==========================

Isolation Level:

* REPEATABLE READ (default)
* SERIALIZABLE (critical flows)

Lock Strategy:

* SELECT ... FOR UPDATE
* Optimistic Lock (version column)

Deadlock Prevention:

* Always lock accounts in sorted order

==================================================
🔁 IDEMPOTENCY DESIGN
=====================

TABLE: transactions

* idempotency_key
* endpoint
* request_hash

FLOW:

1. Check if key exists
2. Compare request_hash
3. If same → return previous result
4. If different → reject (409)

==================================================
💥 FAILURE & RETRY STRATEGY
===========================

* Retry only on safe operations
* Use exponential backoff
* If transaction unknown → reconcile via ledger

==================================================
🛡️ SECURITY BASELINE
=====================

* JWT Authentication
* Role-based access
* Rate limit per user
* Anti double-spend

==================================================
📊 OBSERVABILITY
================

Logging:

* Structured JSON logs
* request_id xuyên suốt

Metrics:

* transaction_success_rate
* transaction_latency
* failure_rate

Tracing:

* OpenTelemetry

==================================================
🧪 TEST STRATEGY
================

1. Unit Test (service logic)
2. Integration Test (DB transaction)
3. Concurrency Test (multi-thread transfer)
4. Idempotency Test
5. Failure recovery test

==================================================
🚀 FUTURE EXTENSIONS
====================

* Kafka (event-driven)
* Fraud detection
* Multi-currency support
* Distributed transactions (Saga)

==================================================
END OF DOCUMENT
===============
