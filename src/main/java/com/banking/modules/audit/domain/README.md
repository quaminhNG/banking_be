# Audit Domain

## Chức năng
Chứa các đối tượng nghiệp vụ (Domain Models), DTOs (Data Transfer Objects), và các hằng số hoặc Enums sử dụng trong Module Audit.

## Các thành phần chính
- `AuditAction`: Enum định nghĩa các loại hành động được kiểm soát (ví dụ: `LOGIN`, `TRANSFER`, `CREATE_ACCOUNT`).
- `AuditResponse`: DTO trả về thông tin audit log cho phía frontend.
- `AuditSearchCriteria`: Đối tượng chứa các tham số dùng để lọc audit logs.
