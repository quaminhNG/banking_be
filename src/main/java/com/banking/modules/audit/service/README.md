# Audit Service

## Chức năng
Thực hiện các logic nghiệp vụ liên quan đến audit logs. Service là lớp trung gian kết nối giữa Controller và Repository.

## Nhiệm vụ
- Xử lý logic lưu trữ audit log (thường được gọi từ các Module khác).
- Tổng hợp và định dạng lại dữ liệu audit trước khi trả về Controller.
- Thực hiện các logic kiểm tra quyền truy cập trước khi cho phép xem log bảo mật.
