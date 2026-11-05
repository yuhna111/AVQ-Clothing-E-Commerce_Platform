-- AVQ Clothing — V1: schema sinh từ Class Diagram (Hình 4.1) qua ORM (Hình 4.2, quy tắc R1–R8)

CREATE TABLE customers (
  customer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(120) UNIQUE,
  phone VARCHAR(15) UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  failed_login_count TINYINT NOT NULL DEFAULT 0,
  locked_until DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_customer_contact CHECK (email IS NOT NULL OR phone IS NOT NULL)
) ENGINE=InnoDB;

CREATE TABLE staff (
  staff_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(100) NOT NULL,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'STAFF',
  failed_login_count TINYINT NOT NULL DEFAULT 0,
  locked_until DATETIME NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE addresses (
  address_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  customer_id BIGINT NOT NULL,
  receiver_name VARCHAR(100) NOT NULL,
  receiver_phone VARCHAR(15) NOT NULL,
  address_line VARCHAR(255) NOT NULL,
  is_default BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_address_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE body_profiles (
  customer_id BIGINT PRIMARY KEY,
  height_cm SMALLINT NOT NULL,
  weight_kg SMALLINT NOT NULL,
  fit_pref ENUM('OM','VUA','RONG') NOT NULL,
  consent_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_profile_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE categories (
  category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  slug VARCHAR(80) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE products (
  product_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_id BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  description TEXT,
  material VARCHAR(80),
  gender ENUM('NAM','NU','UNISEX') NOT NULL DEFAULT 'UNISEX',
  price DECIMAL(12,0) NOT NULL,
  fit_type ENUM('SLIM','REGULAR','OVERSIZE') NOT NULL DEFAULT 'REGULAR',
  is_visible BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(category_id),
  CONSTRAINT ck_price CHECK (price > 0)
) ENGINE=InnoDB;

CREATE TABLE product_images (
  image_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  url VARCHAR(255) NOT NULL,
  is_main BOOLEAN NOT NULL DEFAULT FALSE,
  sort_order TINYINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_image_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE product_variants (
  variant_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  sku VARCHAR(40) NOT NULL UNIQUE,
  size VARCHAR(5) NOT NULL,
  color VARCHAR(30) NOT NULL,
  stock_qty INT UNSIGNED NOT NULL DEFAULT 0,
  CONSTRAINT fk_variant_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
  CONSTRAINT uq_variant UNIQUE (product_id, size, color)
) ENGINE=InnoDB;

CREATE TABLE size_charts (
  chart_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  size VARCHAR(5) NOT NULL,
  height_min SMALLINT NOT NULL, height_max SMALLINT NOT NULL,
  weight_min SMALLINT NOT NULL, weight_max SMALLINT NOT NULL,
  CONSTRAINT fk_chart_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
  CONSTRAINT uq_chart UNIQUE (product_id, size)
) ENGINE=InnoDB;

CREATE TABLE carts (
  cart_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  customer_id BIGINT NOT NULL UNIQUE,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE cart_items (
  cart_id BIGINT NOT NULL,
  variant_id BIGINT NOT NULL,
  quantity TINYINT NOT NULL,
  added_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (cart_id, variant_id),
  CONSTRAINT fk_ci_cart FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
  CONSTRAINT fk_ci_variant FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id),
  CONSTRAINT ck_ci_qty CHECK (quantity BETWEEN 1 AND 10)
) ENGINE=InnoDB;

CREATE TABLE orders (
  order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  customer_id BIGINT NOT NULL,
  order_code VARCHAR(20) NOT NULL UNIQUE,
  order_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  receiver_name VARCHAR(100) NOT NULL,
  receiver_phone VARCHAR(15) NOT NULL,
  shipping_address VARCHAR(255) NOT NULL,
  subtotal DECIMAL(12,0) NOT NULL,
  shipping_fee DECIMAL(12,0) NOT NULL,
  total DECIMAL(12,0) NOT NULL,
  payment_method ENUM('COD','ONLINE') NOT NULL,
  status ENUM('CHO_THANH_TOAN','CHO_XAC_NHAN','DANG_GIAO','DA_GIAO','DA_HUY') NOT NULL,
  expires_at DATETIME NULL,
  CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
) ENGINE=InnoDB;

CREATE TABLE order_items (
  order_id BIGINT NOT NULL,
  variant_id BIGINT NOT NULL,
  product_name VARCHAR(150) NOT NULL,
  size VARCHAR(5) NOT NULL,
  color VARCHAR(30) NOT NULL,
  unit_price DECIMAL(12,0) NOT NULL,
  quantity TINYINT NOT NULL,
  PRIMARY KEY (order_id, variant_id),
  CONSTRAINT fk_oi_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
  CONSTRAINT fk_oi_variant FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id),
  CONSTRAINT ck_oi_qty CHECK (quantity >= 1)
) ENGINE=InnoDB;

CREATE TABLE payments (
  payment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  method ENUM('ONLINE') NOT NULL DEFAULT 'ONLINE',
  amount DECIMAL(12,0) NOT NULL,
  status ENUM('DANG_XU_LY','THANH_CONG','THAT_BAI','SAI_LECH') NOT NULL,
  txn_ref VARCHAR(64),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  paid_at DATETIME NULL,
  CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(order_id)
) ENGINE=InnoDB;

CREATE TABLE order_status_histories (
  history_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  staff_id BIGINT NULL,
  from_status VARCHAR(20),
  to_status VARCHAR(20) NOT NULL,
  note VARCHAR(255),
  changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_osh_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
  CONSTRAINT fk_osh_staff FOREIGN KEY (staff_id) REFERENCES staff(staff_id)
) ENGINE=InnoDB;

CREATE TABLE return_records (
  return_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  variant_id BIGINT NOT NULL,
  reason ENUM('SIZE_LON','SIZE_NHO','KHAC') NOT NULL,
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_rr_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
  CONSTRAINT fk_rr_variant FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id),
  CONSTRAINT fk_rr_staff FOREIGN KEY (created_by) REFERENCES staff(staff_id)
) ENGINE=InnoDB;

CREATE TABLE product_change_logs (
  log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  variant_id BIGINT NULL,
  staff_id BIGINT NOT NULL,
  field_name VARCHAR(30) NOT NULL,
  old_value VARCHAR(255),
  new_value VARCHAR(255),
  changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_log_product FOREIGN KEY (product_id) REFERENCES products(product_id),
  CONSTRAINT fk_log_variant FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id),
  CONSTRAINT fk_log_staff FOREIGN KEY (staff_id) REFERENCES staff(staff_id)
) ENGINE=InnoDB;
