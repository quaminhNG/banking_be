# Banking Backend - Core Engine

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/spring--boot-%236DB33F.svg?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)

Backend service cho hệ thống Core Banking, phục vụ các nghiệp vụ cơ bản như đăng nhập, quản lý số dư (ledger) và xử lý giao dịch nạp/rút/chuyển khoản. Hệ thống được thiết kế hướng tới xử lý đồng thời (high concurrency) tốt, chống deadlock và đảm bảo tính nhất quán dữ liệu ở mức tối đa.

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
5. **Thực hiện stress test với 1000 concurrent transaction requests, đảm bảo zero balance inconsistency nhờ cơ chế Pessimistic Locking và Idempotency.
6. **Kiểm chứng hệ thống trong môi trường high concurrency, đảm bảo tính nhất quán dữ liệu và không phát sinh race condition.
