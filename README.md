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

## Kịch bản demo gợi ý (~10 phút)

| # | Thao tác | Chứng minh | Truy vết tiểu luận |
|---|---|---|---|
| 1 | Trang chủ: lọc **Size M + màu Đen + giá 200.000–500.000**, sắp xếp giá tăng | Lọc kết hợp, chỉ hiện sản phẩm còn hàng | UC03, UC06, FR10, FR11 |
| 2 | Tìm "hoodie" trên thanh tìm kiếm | Tìm theo từ khóa | UC05, FR08 |
| 3 | Mở **Hoodie Basic Nỉ Bông**, nhập 170 cm / 62 kg / Vừa → **Gợi ý** | Gợi ý size; AI Service không chạy nên tự dùng bảng size và lùi 1 size vì phom oversize | UC20, FB-01, AX-02 |
| 4 | Bấm Thêm vào giỏ khi chưa đăng nhập | Chuyển sang đăng nhập | BR03 |
| 5 | Đăng nhập sai mật khẩu 5 lần (tài khoản tự tạo ở bước Đăng ký) | Khóa 15 phút | UC02, BR16 |
| 6 | Đăng nhập `khach@avq.vn`, chọn **XL / Đen** (còn 1), đặt số lượng 2 | Báo "Chỉ còn 1 sản phẩm…" | UC07, BR05, EX-02 |
| 7 | Thêm hoodie L/Đen + áo thun M/Trắng, vào giỏ | Giỏ hàng, sửa số lượng, tạm tính | UC08 |
| 8 | Thanh toán **COD** (tạm tính ≥ 500.000 đ) | Miễn phí giao, đơn **Chờ xác nhận**, tồn kho giảm | UC09, BR09, BR10, BR11 |
| 9 | Đặt đơn thứ hai < 500.000 đ, chọn **trực tuyến** → cổng giả lập → **Thất bại**, rồi **Thanh toán lại** → **Thành công** | Phí 30.000 đ; đơn giữ Chờ thanh toán khi thất bại; IPN ký HMAC chuyển đơn sang Chờ xác nhận | UC10, DT-05, EX-03 |
| 10 | Đơn của tôi → mở một đơn **Chờ xác nhận** → **Hủy đơn** | Hủy, hoàn tồn kho, lịch sử trạng thái | UC11, UC12, BR11, BR13 |
| 11 | `/admin` đăng nhập `nhanvien1` → Đơn hàng → chọn đơn → **Đang giao** + mã vận đơn → **Đã giao** | Chỉ hiện trạng thái kế tiếp hợp lệ; khách xem thấy lịch sử | UC18, UC19, DT-04, BR14 |
| 12 | Thử **Đã hủy** không ghi lý do | Báo bắt buộc lý do | UC19 2a |
| 13 | Sản phẩm → sửa tồn kho một biến thể, sửa giá, ẩn/hiện, thêm sản phẩm mới | Quản lý sản phẩm, nhật ký thay đổi | UC14–UC17, BR06, BR17 |

Mẹo: muốn demo lại từ đầu, xóa dữ liệu (`docker compose down -v`, hoặc `DROP DATABASE avq_db;` với cách 2) rồi chạy lại.

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
| Giao diện báo lỗi mạng khi gọi API | Backend chưa khởi động xong, hoặc chạy frontend bằng cách khác `npm run dev` (cần proxy `/api`) |
| Maven tải thư viện rất lâu lần đầu | Bình thường (~200 MB); lần sau dùng bộ nhớ đệm |
