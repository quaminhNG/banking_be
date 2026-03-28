# Ledger Entity

## Chức năng
Định nghĩa thực thể `LedgerEntry` đóng vai trò là "nguồn sự thật duy nhất" về biến động tiền.

## Các bảng chính
- `ledger_entries`: Chứa các bản ghi biến động (ID, account_id, type, amount, status, v.v.).

## Đặc tính quan trọng
- Dữ liệu ở đây là IMMUTABLE (không được phép sửa hoặc xóa).
- Chỉ cho phép APPEND (thêm mới).