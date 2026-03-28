# Audit Controller

## Chức năng
Thư mục này chứa các REST Controllers chịu trách nhiệm tiếp nhận và phản hồi các yêu cầu HTTP liên quan đến dữ liệu audit.

## Các API chính (Dự kiến)
- `GET /api/v1/audits`: Truy vấn danh sách audit logs với phân trang và bộ lọc.
- `GET /api/v1/audits/{id}`: Xem chi tiết một bản ghi audit cụ thể.
- `GET /api/v1/audits/user/{userId}`: Lịch sử hoạt động của một người dùng nhất định.
