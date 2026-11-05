-- AVQ Clothing — V2: sửa đổi CSDL sau thiết kế lớp (Hình 4.5) và phân tầng (Hình 4.7–4.9)
-- Mỗi thay đổi ghi rõ lớp/phương thức thiết kế đòi hỏi nó (DB-R1 … DB-R7, Mục 4.6)

-- DB-R1: @Version ProductVariant + ProductVariantRepository.decreaseStock() (khóa lạc quan, NFR05)
ALTER TABLE product_variants ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- DB-R2: @Version Order (xung đột khách hủy / nhân viên chuyển trạng thái, AS-11)
ALTER TABLE orders ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- DB-R3: OrderService.updateStatus(..., DANG_GIAO, note) lưu mã vận đơn vào Order.trackingCode
ALTER TABLE orders ADD COLUMN tracking_code VARCHAR(40) NULL AFTER expires_at;

-- DB-R4: Order.recalculate() bảo đảm total = subtotal + shippingFee -> kiểm tra lại ở tầng CSDL
ALTER TABLE orders ADD CONSTRAINT ck_order_total CHECK (total = subtotal + shipping_fee);

-- DB-R5: chỉ mục cho các phương thức repository ở tầng Data access
CREATE INDEX idx_order_expiry   ON orders (status, expires_at);                 -- OrderRepository.findByStatusAndExpiresAtBefore (OrderExpiryScheduler)
CREATE INDEX idx_order_customer ON orders (customer_id, order_date);            -- OrderRepository.findByOrderCodeAndCustomerId / đơn của tôi
CREATE INDEX idx_osh_order      ON order_status_histories (order_id, changed_at); -- OrderStatusHistoryRepository.findByOrderIdOrderByChangedAt
CREATE INDEX idx_log_product    ON product_change_logs (product_id, changed_at);
CREATE INDEX idx_product_cat    ON products (category_id, is_visible);           -- ProductRepository.search (lọc danh mục)
CREATE FULLTEXT INDEX ft_product ON products (name, description);                -- ProductRepository.search (từ khóa, FR08)

-- DB-R6: PaymentService.handleCallback() dùng PaymentRepository.existsByTxnRef() -> mỗi giao dịch chỉ ghi một lần (NFR11)
ALTER TABLE payments ADD CONSTRAINT uq_payment_txn UNIQUE (txn_ref);

-- DB-R7: AiServiceClient / SizeAdvisorService ghi nhật ký suy luận AI (ANFR-08)
CREATE TABLE ai_inference_logs (
  log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  feature ENUM('SIZE','IMAGE') NOT NULL,
  model_version VARCHAR(40) NOT NULL,
  input_hash CHAR(64) NOT NULL,
  output_json JSON,
  latency_ms INT NOT NULL,
  fallback_used BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;
