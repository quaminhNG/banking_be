# Account Service

## Chức năng
Chứa logic nghiệp vụ xử lý tài khoản.

## Luồng xử lý chính
- Tạo tài khoản mới: Khởi tạo ID (UUID), thiết lập trạng thái `ACTIVE`, và phối hợp với module Ledger để tạo entry ban đầu.
- Quản lý trạng thái: Khóa (LOCKED) hoặc đóng (CLOSED) tài khoản.