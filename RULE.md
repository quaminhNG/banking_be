# 🚀 CODING & GIT RULES

==================================================
🧠 1. GENERAL PRINCIPLES
========================

* Code phải dễ đọc hơn là “ngắn”
* Không đoán logic → phải rõ ràng
* Mỗi function chỉ làm 1 việc
* Không duplicate logic (DRY)
* Ưu tiên clarity > cleverness

==================================================
📛 2. NAMING CONVENTION
=======================

## 🔹 Class

* PascalCase
* Ví dụ:

  * AccountService
  * LedgerEntry
  * TransferController

## 🔹 Variable

* camelCase
* Ví dụ:

  * accountId
  * currentBalance
  * transactionStatus

## 🔹 Method

* camelCase + verb rõ nghĩa
* Ví dụ:

  * createAccount()
  * calculateBalance()
  * processTransfer()

## 🔹 Boolean

* prefix: is / has / can
* Ví dụ:

  * isActive
  * hasBalance
  * canWithdraw

## 🔹 Constant

* UPPER_CASE
* Ví dụ:

  * MAX_RETRY
  * DEFAULT_CURRENCY

==================================================
📦 3. PACKAGE NAMING
====================

* lowercase
* không viết tắt khó hiểu

Ví dụ:

* com.banking.modules.account
* com.banking.modules.ledger

==================================================
🧩 4. CODE STRUCTURE RULE
=========================

## Controller

* Chỉ nhận request + validate basic
* Không viết business logic

## Service

* Chứa business logic
* Điều phối flow

## Repository

* Chỉ query DB
* Không có logic

## Domain

* Logic thuần (pure function)
* Không phụ thuộc framework

==================================================
🧾 5. METHOD RULE
=================

* Không quá 30–40 dòng
* Tên method phải mô tả được logic
* Tránh nested if quá sâu

❌ Sai:
doSomething()

✅ Đúng:
calculateAccountBalance()

==================================================
💥 6. ERROR HANDLING
====================

* Không throw Exception chung chung
* Luôn dùng BusinessException
* Phải có ErrorCode

Ví dụ:
throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);

==================================================
💾 7. DATABASE RULE
===================

* Không update trực tiếp số dư
* Chỉ INSERT vào ledger
* Luôn dùng transaction (@Transactional)

==================================================
🧪 8. TESTING RULE
==================

* Service phải có unit test
* Case quan trọng:

  * success
  * fail
  * edge case

==================================================
🔀 9. GIT BRANCH RULE
=====================

## Naming:

* feature/<name>
* fix/<name>
* hotfix/<name>
* refactor/<name>

Ví dụ:

* feature/deposit-api
* fix/withdraw-bug

==================================================
📝 10. COMMIT MESSAGE RULE
==========================

Format:

<type>: <short description>

## Types:

* feat: thêm feature mới
* fix: sửa bug
* refactor: sửa code không đổi logic
* chore: config, setup
* test: thêm/sửa test
* docs: update tài liệu
* perf: tối ưu performance

==================================================
📌 Ví dụ commit

feat: add deposit api
fix: handle insufficient balance error
refactor: split transfer logic into service
docs: update API contract
perf: optimize ledger query

==================================================
🚫 KHÔNG VIẾT

* "update code"
* "fix bug"
* "abc"
* commit không rõ nghĩa

==================================================
🔥 11. CODE REVIEW RULE
=======================

* Không approve nếu:

  * code khó đọc
  * logic không rõ
  * thiếu test

* Luôn hỏi:

  * code này có dễ hiểu không?
  * có thể bug ở đâu?

==================================================
🎯 FINAL RULE
=============

"Code cho người đọc, không phải cho máy"

==================================================
END
===
