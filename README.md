# Banking Backend - Core Engine

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/spring--boot-%236DB33F.svg?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)

Đây là backend service cho hệ thống Core Banking, phục vụ các nghiệp vụ cơ bản như đăng nhập, quản lý số dư (ledger) và xử lý giao dịch nạp/rút/chuyển khoản. Hệ thống được thiết kế hướng tới xử lý đồng thời (high concurrency) tốt, chống deadlock và đảm bảo tính nhất quán dữ liệu ở mức tối đa.

## Tech Stack
- **Framework:** Java 21, Spring Boot 3
- **Database:** PostgreSQL
- **Security:** Spring Security, JWT
- **Build tool:** Maven

## Tính năng nổi bật
1. **Idempotency (Tính luỹ đẳng):** Tất cả các API giao dịch (Nạp/Rút/Chuyển khoản) đều yêu cầu `idempotencyKey` ở body để chống duplicate request (ví dụ user bấm 2 lần do mạng lag).
2. **Concurrency Control:** Sử dụng Database Pessimistic Locking để khoá row theo thứ tự ID, chống deadlock khi có hàng trăm request chuyển khoản đan chéo nhau cùng lúc.
3. **Data Seeding:** App tự động sinh sẵn dữ liệu 2 users (`testuser1`, `testuser2` - pass `123456`) để dev có thể test ngay mà không cần setup rườm rà.
4. **Third-party Bank Integration:** Hỗ trợ định tuyến routing giả lập API sang các ngân hàng đối tác (VCB, TCB, MBBank,...).

## Setup & Chạy Local

**1. Chuẩn bị:**
Tạo 1 database tên `banking_db` trong PostgreSQL:
```sql
CREATE DATABASE banking_db;
```

**2. Chạy app:**
```bash
mvn clean install
mvn spring-boot:run
```
App sẽ listen ở cổng `8080`.

**3. Chạy Test:**
Dự án có sẵn Integration test ép tải 1000 giao dịch đồng thời để chứng minh tính vẹn toàn dữ liệu. Chạy bằng lệnh:
```bash
mvn test -Dtest=TransferConcurrencyTest
```

## API Căn Bản

*Chi tiết các request body, response code (thành công/thất bại) có thể tham khảo trực tiếp ở file `output.md` trong source.*

Luồng test cơ bản:
1. **Lấy Token:** `POST /api/v1/auth/login` (Dùng account seeder ở trên) -> Sinh ra JWT, lấy mã này gắn vào Header `Authorization: Bearer <token>`.
2. **Check tiền:** `GET /api/v1/ledger/balance/{accountId}`
3. **Nạp tiền:** `POST /api/v1/transaction/deposit` (truyền số tiền và `idempotencyKey`).
4. **Chuyển tiền:** `POST /api/v1/transaction/transfer` (Nhập đầy đủ tài khoản gửi, người nhận, số tiền. Trỏ `toBankCode` = rỗng nếu nội mạng, = `VCB` nếu ngoại mạng).
