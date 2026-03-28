# Modules Layer (Core)

Chia hệ thống theo domain:

- account
- ledger
- transaction
- transfer

Mỗi module là 1 hệ thống độc lập.

Nguyên tắc:
- Không gọi repository module khác
- Chỉ giao tiếp qua service