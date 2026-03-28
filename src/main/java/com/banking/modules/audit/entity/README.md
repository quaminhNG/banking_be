# Audit Entity

## Chức năng
Định nghĩa các thực thể JPA (Java Persistence API) ánh xạ trực tiếp với các bảng trong cơ sở dữ liệu.

## Thực thể chính
- `AuditLog`: Ánh xạ tới bảng `audit_logs`.
    - `id`: Định danh duy nhất (UUID).
    - `requestId`: Liên kết với yêu cầu HTTP để truy vết luồng xử lý.
    - `userId`: ID người dùng thực hiện hành động.
    - `action`: Tên hành động thực hiện.
    - `metadata`: Dữ liệu JSON chứa thông tin chi tiết (ví dụ: giá trị trước và sau khi đổi).
    - `createdAt`: Thời điểm ghi log.
