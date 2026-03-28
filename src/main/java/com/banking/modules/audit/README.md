# Module Audit

## Giới thiệu
Module Audit chịu trách nhiệm ghi lại và quản lý các nhật ký kiểm soát (audit logs) của toàn bộ hệ thống. Đây là một thành phần quan trọng để đảm bảo tính minh bạch, hỗ trợ truy vết các hành động của người dùng và các thay đổi dữ liệu quan trọng.

## Nguyên tắc
- **Tính toàn vẹn**: Mọi thay đổi quan trọng trong hệ thống (giao dịch, thay đổi trạng thái tài khoản, v.v.) phải được ghi lại.
- **Tính bất biến**: Nhật ký audit sau khi ghi không được phép chỉnh sửa hoặc xóa.
- **Chi tiết**: Ghi lại đầy đủ thông tin: ai làm (user_id), khi nào (created_at), hành động gì (action), và dữ liệu liên quan (metadata).

## Cấu trúc thư mục
- `controller/`: Cung cấp các API để truy vấn nhật ký audit.
- `domain/`: Chứa các yêu cầu logic hoặc DTO liên quan đến audit.
- `entity/`: Định nghĩa thực thể `AuditLog` ánh xạ với cơ sở dữ liệu.
- `repository/`: Giao tiếp với cơ sở dữ liệu để lưu trữ và truy vấn audit logs.
- `service/`: Chứa logic nghiệp vụ xử lý dữ liệu audit.
