# CLAUDE.md — AVQ Clothing

Website bán quần áo dùng làm **demo cho tiểu luận "Phân tích thiết kế hệ thống thông tin"** (sinh viên Nguyễn An Huy, PTIT).
Mọi thay đổi mã phải giữ khớp với thiết kế trong tiểu luận (Chương 4–5): kiến trúc phân tầng, tên lớp/bảng, quy tắc nghiệp vụ BRxx.

## Cấu trúc
- `frontend/` — React 18 + Vite. `npm install && npm run dev` (cổng 5173, proxy `/api` → `http://localhost:8080`, xem `vite.config.js`).
  Trang khách: `src/pages/*`, trang nhân viên: `src/admin/*`, gọi API qua `src/api.js`. Ảnh sản phẩm SVG ở `public/images`.
- `backend/` — **hiện là Java 17 + Spring Boot 3.2** (kiến trúc 5 tầng: `api` → `service` → `repository` → `domain`, cộng `infra`, `config`).
  Chạy: `mvn spring-boot:run`. Kiểm thử: `mvn test` (28 test JUnit 5, repository giả lập bằng proxy trong `src/test/.../support`).
- `backend/src/main/resources/db/migration/V1__initial_schema.sql`, `V2__design_revisions.sql` — CSDL (Hình 4.3 và 4.10 của tiểu luận).
  `db/demo/V3__demo_seed.sql` — dữ liệu demo (8 sản phẩm, 64 biến thể; khách `khach@avq.vn`/`matkhau123`, nhân viên `nhanvien1`/`staff@123`, mật khẩu BCrypt).
- `docs/API.md` — **hợp đồng API đầy đủ** (endpoint, DTO, mã lỗi, quy tắc nghiệp vụ). Frontend phụ thuộc đúng hợp đồng này.
- `docker-compose.yml` — MySQL 8 + backend + frontend (nginx).

## Việc dự kiến: chuyển backend sang Node.js
Mục tiêu: chạy cả hai bằng `npm run dev` (frontend Vite, backend Node), không cần Java.
- Tạo `backend-node/` (Express + `mysql2/promise`, ES modules), cổng 8080, script `dev` dùng `node --watch src/server.js`.
- **Giữ nguyên hợp đồng `docs/API.md`** để frontend không phải sửa (kể cả tên trường JSON, mã lỗi, mã HTTP).
- **Giữ kiến trúc phân tầng của tiểu luận**: `src/api` (router/controller) → `src/service` → `src/repository` (SQL) → `src/domain`; `src/infra` (JWT, cổng thanh toán giả lập, scheduler); `src/config`.
  Đặt tên file/hàm theo lớp Java tương ứng (OrderService.place, OrderStateMachine.transit, StockService.reserve/restore, PaymentService.handleCallback, FallbackPolicy.ruleBasedSize…).
- **CSDL dùng lại nguyên các file V1, V2, V3** (chạy theo thứ tự khi khởi động nếu bảng chưa có — có thể ghi version đã chạy vào bảng `schema_version`). Không tự đổi schema.
- Giao dịch: dùng `connection.beginTransaction()`; đặt hàng phải **trừ tồn kho trước khi insert đơn** (tránh deadlock InnoDB — đã kiểm chứng ở Mục 5.5.3 của tiểu luận).
- Mật khẩu: `bcryptjs` (hash trong seed là `$2b$10$...`). JWT HS256: `jsonwebtoken` hoặc tự ký bằng `crypto`.
- Tiền: lưu DECIMAL(12,0), trả về số nguyên. Ngày giờ: ISO không có timezone (Asia/Ho_Chi_Minh).
- Viết kiểm thử (`node --test` hoặc vitest) cho các quy tắc: phí giao (BR09), DT-02 giỏ hàng, máy trạng thái DT-04, hoàn tồn khi hủy, khóa sau 5 lần sai, IPN (00/02/04/97), hết hạn 15 phút, 10 khách tranh chiếc cuối (chỉ 1 thành công).
- Sau khi chuyển: cập nhật README (cách chạy) và `docker-compose.yml`; báo lại những chỗ tiểu luận (Chương 4.7, Chương 5) cần sửa vì đổi ngôn ngữ.

## Quy tắc nghiệp vụ quan trọng (tóm tắt)
BR03 xem/tìm không cần đăng nhập; giỏ/đơn cần đăng nhập · BR04 phải chọn size+màu · BR05 1–10 sản phẩm/dòng, không vượt tồn · BR07 cộng dồn ·
BR08 chốt giá lúc đặt · BR09 phí 30.000 đ, miễn phí từ 500.000 đ · BR10 COD → Chờ xác nhận; ONLINE → Chờ thanh toán · BR11 trừ tồn khi tạo đơn, hoàn khi hủy ·
BR12 hủy đơn chờ thanh toán quá 15 phút · BR13 khách chỉ hủy khi Chờ thanh toán/Chờ xác nhận · BR14 bảng chuyển trạng thái DT-04 · BR16 khóa 15 phút sau 5 lần sai · BR17 sản phẩm có ≥ 1 biến thể.
