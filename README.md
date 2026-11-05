# AVQ Clothing — Demo website bán quần áo

Hệ thống gồm 3 phần, đúng kiến trúc ở Chương 4–5 của tiểu luận:

| Phần | Công nghệ | Thư mục | Cổng |
|---|---|---|---|
| CSDL | MySQL 8 (hoặc MariaDB/XAMPP) — Flyway tự chạy V1, V2 và V3 (dữ liệu demo) | `backend/src/main/resources/db` | 3306 |
| Backend | Java 17, Spring Boot 3.2 | `backend` | 8080 |
| Giao diện | React 18 + Vite | `frontend` | 5173 |

Tài khoản demo: **khách** `khach@avq.vn` / `matkhau123` · **nhân viên** `nhanvien1` / `staff@123` (trang `/admin`).

---

## Cách 1 — Docker (khuyên dùng, chỉ cần cài Docker Desktop)

```bash
cd avq-clothing
docker compose up --build
```

Lần đầu mất 5–10 phút để tải thư viện. Khi thấy dòng `Started AvqShopApplication`, mở **http://localhost:5173**.
Dừng: `Ctrl + C`, sau đó `docker compose down` (thêm `-v` nếu muốn xóa sạch dữ liệu để demo lại từ đầu).

## Cách 2 — Chạy từng phần (Java 17 + Maven/IntelliJ, MySQL hoặc XAMPP, Node.js 18+)

1. **CSDL**: bật MySQL (XAMPP: Start MySQL). Không cần tạo bảng — backend tự tạo CSDL `avq_db` và chạy migration.
   Nếu MySQL có mật khẩu root, đặt biến môi trường `DB_PASSWORD=...` trước bước 2.
2. **Backend**:
   ```bash
   cd backend
   mvn spring-boot:run          # hoặc mở thư mục backend bằng IntelliJ rồi Run AvqShopApplication
   ```
3. **Giao diện** (cửa sổ terminal khác):
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   Mở **http://localhost:5173** (Vite tự chuyển `/api` sang backend cổng 8080).

Kiểm thử đơn vị: `cd backend && mvn test` (28 test JUnit 5).

### Hướng dẫn chi tiết cho Windows (đã chạy thử với Java 24, Node 24, MySQL 8.0)

**a) Cài Maven nếu máy chưa có** (`mvn -v` báo không tìm thấy lệnh). Không cần cài đặt, chỉ cần tải bản zip và giải nén, ví dụ vào `C:\Users\<tên>\tools`:
```powershell
mkdir $HOME\tools -Force; cd $HOME\tools
curl.exe -LO https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip
Expand-Archive apache-maven-3.9.9-bin.zip .
& "$HOME\tools\apache-maven-3.9.9\bin\mvn" -v
```
Sau đó dùng đường dẫn đầy đủ `$HOME\tools\apache-maven-3.9.9\bin\mvn` thay cho `mvn`, hoặc thêm thư mục `bin` đó vào biến môi trường `PATH`.
Máy chỉ có JDK mới hơn 17 (21, 24…) vẫn chạy được.

**b) Kiểm tra mật khẩu root MySQL**:
```powershell
mysql -uroot -p        # gõ mật khẩu; vào được dấu nhắc "mysql>" là đúng
```
Nếu quên mật khẩu (dịch vụ `MySQL80`), mở PowerShell bằng **Run as Administrator** để đặt lại, ví dụ thành `avq123`:
```powershell
Stop-Service MySQL80
Set-Content C:\mysql-init.txt "ALTER USER 'root'@'localhost' IDENTIFIED BY 'avq123';"
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe" --defaults-file="C:\ProgramData\MySQL\MySQL Server 8.0\my.ini" --init-file=C:\mysql-init.txt --console
# thấy dòng "ready for connections" thì bấm Ctrl+C, rồi:
Start-Service MySQL80
Remove-Item C:\mysql-init.txt
```

**c) Chạy hệ thống** bằng 2 cửa sổ PowerShell:
```powershell
# Cửa sổ 1 — backend (đợi dòng "Started AvqShopApplication")
cd <thư mục dự án>\avq-clothing\backend
$env:DB_PASSWORD="mat_khau_root_mysql"   # thay bằng mật khẩu root MySQL của bạn
mvn spring-boot:run
```
```powershell
# Cửa sổ 2 — giao diện
cd <thư mục dự án>\avq-clothing\frontend
npm install        # chỉ cần lần đầu
npm run dev
```
Mở **http://localhost:5173**. Lần đầu khởi động, backend tự tạo CSDL `avq_db` và chạy V1, V2, V3 (log có dòng `Successfully applied 3 migrations`).

**d) Demo lại từ đầu** (xóa dữ liệu cũ), rồi khởi động lại backend:
```powershell
mysql -uroot -p -e "DROP DATABASE avq_db;"
```

Lưu ý: API nằm dưới `/api/v1` (ví dụ `http://localhost:8080/api/v1/products`), không phải `/api/products`.

---


## Thanh toán VNPay thật (tùy chọn)

Mặc định `payment.provider=mock` dùng cổng giả lập (cùng thuật toán ký HMAC-SHA512 như VNPay, đi qua đúng `PaymentService.handleCallback`).
Để dùng VNPay sandbox: đăng ký tại sandbox.vnpayment.vn, rồi đặt `PAYMENT_PROVIDER=vnpay`, `VNPAY_TMN_CODE`, `VNPAY_HASH_SECRET`; IPN URL phải truy cập được từ Internet (ví dụ dùng ngrok).

## Xử lý lỗi thường gặp

| Lỗi | Cách xử lý |
|---|---|
| `Port 3306/8080/5173 is already in use` | Tắt chương trình đang dùng cổng (XAMPP, phiên chạy cũ) hoặc đổi cổng trong `docker-compose.yml` |
| `Access denied for user 'root'` | Đặt đúng `DB_PASSWORD` (cách 2) |
| `Public Key Retrieval is not allowed` | URL mặc định đã có `allowPublicKeyRetrieval=true`; nếu tự đặt `DB_URL` hãy thêm tham số này |
| Flyway báo `Validate failed` / `checksum mismatch` | CSDL còn từ lần chạy cũ với script khác: xóa CSDL `avq_db` rồi chạy lại |
| Ảnh sản phẩm tải lên không hiển thị (ảnh mẫu `.svg` vẫn hiện) | Đang mở một bản `npm run dev` cũ hoặc chạy ở cổng khác 5173. Tắt mọi cửa sổ `npm run dev`, chạy lại một bản duy nhất, mở đúng **http://localhost:5173** và bấm Ctrl+F5 |
| Giao diện báo lỗi mạng khi gọi API | Backend chưa khởi động xong, hoặc chạy frontend bằng cách khác `npm run dev` (cần proxy `/api`) |
| Maven tải thư viện rất lâu lần đầu | Bình thường (~200 MB); lần sau dùng bộ nhớ đệm |
