# Transaction Module

Quản lý luồng giao dịch tài chính:

- **Deposit**: Nạp tiền vào tài khoản.
- **Withdraw**: Rút tiền từ tài khoản.
- **Idempotency**: Chống xử lý trùng lặp request.
- **Status Tracking**: Theo dõi trạng thái (PENDING, SUCCESS, FAILED).

Module này không trực tiếp cộng/trừ tiền mà gọi xuống **Ledger Module** sau khi kiểm soát request thành công.