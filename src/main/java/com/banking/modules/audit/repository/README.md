# Audit Repository

## Chức năng
Chứa các interface kế thừa từ Spring Data JPA (như `JpaRepository`) để thực hiện các thao tác CRUD và truy vấn nâng cao trên bảng `audit_logs`.

## Nhiệm vụ
- Lưu trữ các bản ghi audit mới một cách nhanh chóng.
- Hỗ trợ các câu lệnh truy vấn lọc theo thời gian, người dùng, hoặc loại hành động.
