# 🏦 Banking Backend System

Dự án Hệ thống Core Banking Backend tập trung vào việc mô phỏng các nghiệp vụ cốt lõi của ngân hàng như: Đăng ký/Đăng nhập, Quản lý số dư, Nạp/Rút tiền và Chuyển khoản (Nội bộ & Liên ngân hàng).

## ✨ Các Chức Năng Chính

1. **Quản lý Định danh (Authentication):**
   - Đăng ký tài khoản người dùng mới.
   - Đăng nhập và nhận chuỗi bảo mật (JWT Token) để sử dụng cho các chức năng sau.
2. **Quản lý Tài khoản (Account & Ledger):**
   - Mỗi người dùng ngay sau khi tạo định danh sẽ được cấp tài khoản ngân hàng.
   - Tra cứu số dư tài khoản lập tức và an toàn.
3. **Giao dịch (Transactions):**
   - **Nạp tiền (Deposit):** Tạo lệnh biến động số dư dương.
   - **Rút tiền (Withdraw):** Tạo lệnh biến động số dư âm (Sẽ có rào chắn kiểm tra đảm bảo số lượng tiền rút không được vượt số tiền hiện có).
   - **Chuyển khoản (Transfer):** 
     - *Nội bộ:* Gửi tiền qua lại giữa các user trong cùng ứng dụng.
     - *Ngoại mạng:* Gửi tiền sang các ngân hàng đối tác bằng API giả lập Sandbox (VD: VCB, TCB, MB, v.v.).

*(Lưu ý chống ngập lụt tài chính/Spam: Mọi API Nạp/Rút/Chuyển bắt buộc đính kèm 1 KEY id tạm thời tên là `idempotencyKey` để chống trường hợp mạng lag gửi lệnh trừ tiền lên server 2 lần)*.

## 🚀 Hướng Dẫn Cài Đặt & Chạy Ứng Dụng

**Yêu cầu:** Máy tính cài đặt sẵn Java 21, Maven và PostgreSQL chạy ở cổng mặc định.

1. **Chuẩn bị DB:** Tạo database (ví dụ tên `banking_db`) trong PostgreSQL hợp lệ.
2. **Khởi chạy ứng dụng:** Mở terminal ngay tại thư mục của dự án và gõ:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
3. Sau khi bật thành công, Server đón nhận request tại địa chỉ: `http://localhost:8080`

## 🛠 Hướng Dẫn Sử Dụng Chi Tiết (API Workflow)

Sau khi server chạy lên, bạn có thể sử dụng cURL, Postman hoặc gọi HTTP từ front-end theo thứ tự logic dưới đây:

### Bước 1: Khởi tạo tài khoản (Register)
Tạo account cho mình:
- **POST** `/api/v1/auth/register`
- **Body:** 
  ```json
  { "username": "nguyenvana", "password": "Password@123" }
  ```

### Bước 2: Lấy chìa khóa giao dịch (Login)
- **POST** `/api/v1/auth/login`
- **Body:** Sử dụng lại username và password ở bước 1.
- **Kết quả:** Hệ thống sẽ trả cho bạn `token` và Id tài khoản `accountId`. 
> ⚠️ **Chú ý:** Các bước 3, 4, 5 ở dưới thay đổi lên dữ liệu thật, nên bắt buộc bạn phải thêm khóa này vào trong Header:
  > `Authorization: Bearer <Đoạn_Mã_Token_Lấy_Được_Ở_Đây>`

### Bước 3: Thử nạp tiền (Deposit)
Nạp 1,000,000 VNĐ vào tài khoản vừa tạo.
- **POST** `/api/v1/transaction/deposit`
- **Body:** 
  ```json
  { 
    "accountId": "<accountId của bạn lấy ở bước 2>", 
    "amount": 1000000, 
    "idempotencyKey": "giao_dich_nap_dau_tien" 
  }
  ```

### Bước 4: Kiểm tra lại số tiền (Get Balance)
- **GET** `/api/v1/ledger/balance/{accountId_của_bạn}`
- Dữ liệu trả về sẽ show `"balance": 1000000`.

### Bước 5: Chuyển khoản (Transfer)
Chuyển đi 50,000 VNĐ cho tài khoản nội bộ khác.
- **POST** `/api/v1/transaction/transfer`
- **Body:** 
  ```json
  {
    "fromAccountId": "<accountId_cua_ban>",
    "toAccountId": "<accountId_nguoi_nhan_tien>",
    "amount": 50000,
    "currency": "VND",
    "idempotencyKey": "trans_50k_1",
    "toBankCode": ""
  }
  ```
*(Mẹo nhỏ liên ngân hàng: Nếu bạn muốn test lệnh chuyển đi ra ngoài ngân hàng khác, bạn chỉ cần điều chỉnh `toBankCode` không để trắng nữa mà nhập tên ngân hàng ví dụ như `"VCB"`, `"TCB"`, hệ thống sẽ tự định tuyến tới module adapter kết nối Sandbox của bank đó).*

---

> 📖 **Xem Các Tình Huống Chi Tiết Hơn:** 
> Báo cáo cụ thể về tất cả tham số Headers, Response Codes (Kể cả bị lỗi 400 do không đủ tiền, lỗi 403 khi thao tác trái phép) đã được tôi ghi xuất đầy đủ trong file **`output.md`**. Bạn có thể đọc nó để biết API trả về gì nhé!
