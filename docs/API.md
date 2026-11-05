# Hợp đồng API — AVQ Clothing (frontend `frontend/src` đang gọi đúng các API này)

Tiền tố: `/api/v1`. Dữ liệu JSON. Xác thực: header `Authorization: Bearer <JWT>` (JWT HS256, payload `{sub, role, exp}`, hạn 30 phút).
Lỗi luôn có dạng `{ "code": "...", "message": "..." }` với mã HTTP:
400 VALIDATION_ERROR, VARIANT_REQUIRED, CANCEL_REASON_REQUIRED · 401 AUTH_REQUIRED, AUTH_FAILED, ACCOUNT_LOCKED · 403 FORBIDDEN · 404 NOT_FOUND ·
409 CART_EMPTY, OUT_OF_STOCK, QTY_LIMIT, PRICE_CHANGED, INVALID_TRANSITION, DUPLICATE.

Phân quyền: `/admin/**` chỉ role STAFF; `/cart`, `/orders`, `/addresses` chỉ role CUSTOMER; còn lại công khai.
Ngày giờ trả về dạng ISO `2026-11-05T10:30:00`. Tiền là số nguyên VNĐ.

## Tài khoản
| Method | Path | Body | Trả về |
|---|---|---|---|
| POST | /auth/register | `{fullName, email, phone, password}` (≥ 8 ký tự; email hoặc phone; không trùng → DUPLICATE) | `TokenDto` |
| POST | /auth/login | `{identifier, password}` (identifier = email hoặc SĐT) | `TokenDto` |
| POST | /auth/staff/login | `{identifier, password}` (identifier = username) | `TokenDto` |

`TokenDto = {accessToken, role: "CUSTOMER"|"STAFF", expiresInSeconds, fullName}`.
BR16: sai 5 lần liên tiếp → khóa 15 phút (lần thứ 5 trả ACCOUNT_LOCKED); đăng nhập đúng thì reset bộ đếm.

## Sản phẩm (công khai)
| Method | Path | Tham số | Trả về |
|---|---|---|---|
| GET | /categories | – | `[{id, name, slug}]` |
| GET | /products | query `q, categoryId, size, color, minPrice, maxPrice, sort` (`price_asc`, `price_desc`, mặc định mới nhất) | `[ProductCardDto]` |
| GET | /products/{id} | – (sản phẩm ẩn → 404) | `ProductDetailDto` |
| POST | /products/{id}/size-advice | `{heightCm, weightKg, fitPref: "OM"|"VUA"|"RONG", saveProfile}` | `{size, confidence, reason, source: "AI"|"CHART"|"NONE"}` |

`ProductCardDto = {id, name, category, price, image, sizes[] (size còn hàng, thứ tự S,M,L,XL), colors[], totalStock}` — chỉ sản phẩm hiển thị và có biến thể khớp bộ lọc còn hàng.
`ProductDetailDto = {id, name, category, description, material, fitType, price, visible, images[], variants:[{id, sku, size, color, stock}], hasSizeChart}`.
Gợi ý size khi không có AI Service (FB-01): chọn size đầu tiên trong `size_charts` có khoảng chiều cao, cân nặng chứa số đo; ngoài bảng thì theo chiều cao gần nhất; phom OVERSIZE + kiểu VUA thì lùi 1 size; `source = "CHART"`, `reason = "Theo bảng size"`. Không có bảng size → `{size: null, source: "NONE", reason: "Liên hệ cửa hàng để được tư vấn size"}`.

## Giỏ hàng (CUSTOMER)
| Method | Path | Body / tham số | Trả về |
|---|---|---|---|
| GET | /cart | – | `CartDto` |
| POST | /cart/items | `{variantId, quantity}` | `CartDto` |
| PATCH | /cart/items/{variantId}?qty=N | – | `CartDto` |
| DELETE | /cart/items/{variantId} | – | `CartDto` |

`CartDto = {totalQuantity, subtotal, items:[{variantId, productId, productName, size, color, image, unitPrice, quantity, stock, lineTotal}]}`.
Quy tắc (DT-02): thiếu variantId → VARIANT_REQUIRED; mỗi biến thể một dòng, thêm lại thì cộng dồn (BR07); số lượng dòng > 10 → QTY_LIMIT (BR05);
vượt tồn kho → OUT_OF_STOCK với message `"Chỉ còn N sản phẩm size S màu C"`.

## Địa chỉ (CUSTOMER)
GET /addresses → `[{id, receiverName, receiverPhone, addressLine}]` · POST /addresses `{receiverName, receiverPhone, addressLine}` → danh sách mới.

## Đơn hàng (CUSTOMER)
| Method | Path | Body | Trả về |
|---|---|---|---|
| POST | /orders/prepare | – | `{subtotal, shippingFee, total}` (kiểm tra tồn kho từng dòng; giỏ rỗng → CART_EMPTY) |
| POST | /orders | `{addressId, paymentMethod: "COD"|"ONLINE", expectedTotal}` | `{orderCode, status, subtotal, shippingFee, total, expiresAt, paymentUrl}` |
| GET | /orders | – | `[{orderCode, orderDate, status, paymentMethod, total, itemCount, receiverName}]` mới nhất trước |
| GET | /orders/{code} | – | `OrderDetailDto` |
| POST | /orders/{code}/cancel | `{reason}` | `OrderDetailDto` |

Quy tắc đặt hàng (UC09, trong MỘT giao dịch): chốt giá hiện hành vào order_items (BR08) → phí giao 30.000 đ, miễn phí khi tạm tính ≥ 500.000 đ (BR09)
→ tổng khác expectedTotal → PRICE_CHANGED → **trừ tồn kho TRƯỚC khi insert đơn** bằng
`UPDATE product_variants SET stock_qty = stock_qty - ?, version = version + 1 WHERE variant_id = ? AND stock_qty >= ?` (0 dòng → OUT_OF_STOCK, rollback; làm ngược thứ tự sẽ deadlock)
→ COD: status `CHO_XAC_NHAN`; ONLINE: `CHO_THANH_TOAN`, `expires_at = now + 15 phút`, tạo payment `DANG_XU_LY`, `paymentUrl = "/thanh-toan-gia-lap?order=<code>&amount=<total>"`
→ ghi order_status_histories (from NULL → status, "Tạo đơn") → xóa các dòng đã đặt khỏi giỏ. Mã đơn: `AVQ-yyMMddHHmmss` + 2 số.

`OrderDetailDto = {orderCode, status, paymentMethod, orderDate, expiresAt, receiverName, receiverPhone, shippingAddress, trackingCode, subtotal, shippingFee, total,
items:[{productName, size, color, unitPrice, quantity, lineTotal}], history:[{fromStatus, toStatus, note, staffName, changedAt}], nextStatuses[], canCancel}`.

## Máy trạng thái đơn (DT-04, BR13, BR14) — mọi thay đổi trạng thái đi qua một hàm duy nhất
CHO_THANH_TOAN → CHO_XAC_NHAN | DA_HUY · CHO_XAC_NHAN → DANG_GIAO | DA_HUY · DANG_GIAO → DA_GIAO · DA_GIAO, DA_HUY: kết thúc.
Sai → INVALID_TRANSITION. Chuyển sang DA_HUY: hoàn tồn kho (`stock_qty + qty`), xóa expires_at. Nhân viên hủy phải có lý do (CANCEL_REASON_REQUIRED).
Mỗi lần chuyển ghi một dòng order_status_histories (staff_id khi nhân viên làm). Khách chỉ hủy được ở CHO_THANH_TOAN, CHO_XAC_NHAN.
Tác vụ định kỳ mỗi 60 giây: đơn CHO_THANH_TOAN có expires_at < now → DA_HUY "Hết hạn thanh toán 15 phút" (BR12).

## Thanh toán
| Method | Path | Mô tả |
|---|---|---|
| POST | /payments/mock/{code}?success=true\|false | Cổng giả lập: nếu đơn CHO_THANH_TOAN chưa có payment DANG_XU_LY thì tạo mới; sinh tham số IPN ký HMAC-SHA512 rồi xử lý như IPN thật. Trả `{rspCode, message, orderStatus}` |
| GET | /payments/vnpay/ipn | IPN VNPay thật (tùy chọn) |

Xử lý IPN (DT-05): sai chữ ký → `97`; txn_ref đã ghi → `02`; không có đơn → `01`; số tiền khác total → payment SAI_LECH, `04`;
thành công (`vnp_ResponseCode = 00`) → payment THANH_CONG, đơn CHO_THANH_TOAN → CHO_XAC_NHAN; thất bại → payment THAT_BAI, đơn giữ nguyên; trả `00`.

## Quản trị (STAFF)
| Method | Path | Body | Trả về |
|---|---|---|---|
| GET | /admin/orders?status= | – | danh sách như GET /orders (status rỗng = tất cả) |
| GET | /admin/orders/{code} | – | `OrderDetailDto` |
| PATCH | /admin/orders/{code}/status | `{status, note}` (DANG_GIAO: note = mã vận đơn → tracking_code; đơn CHO_THANH_TOAN → INVALID_TRANSITION) | `OrderDetailDto` |
| GET | /admin/products | – | `[ProductDetailDto]` gồm cả sản phẩm ẩn |
| POST | /admin/products | `{categoryId, name, description, material, fitType, price, imageUrl, variants:[{size, color, sku, stock}]}` (≥ 1 biến thể) | `ProductDetailDto` |
| PUT | /admin/products/{id} | `{price?, description?, material?, visible?}` (price > 0) | `ProductDetailDto` |
| PATCH | /admin/variants/{variantId}/stock | `{stock}` (số nguyên ≥ 0) — ghi product_change_logs (field_name `stock_qty`, giá trị cũ/mới) | 200 rỗng |
