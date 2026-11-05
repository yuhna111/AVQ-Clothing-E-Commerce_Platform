package vn.avq.shop.db;

import java.sql.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Kiểm thử tích hợp trên MariaDB/MySQL thật: chạy đúng câu lệnh của ProductVariantRepository.decreaseStock
 * trong giao dịch tạo đơn, với 10 khách đặt đồng thời chiếc áo cuối cùng (NFR05, BR11).
 */
public class StockConcurrencyIT {
    static final String URL = System.getProperty("db.url", "jdbc:mariadb://localhost:3306/avq_db");

    public static void main(String[] args) throws Exception {
        try (Connection c = DriverManager.getConnection(URL, "root", "")) {
            c.createStatement().executeUpdate("UPDATE product_variants SET stock_qty = 1 WHERE sku = 'HD-BSC-XL-DE'");
            c.createStatement().executeUpdate("DELETE FROM orders WHERE order_code LIKE 'AVQ-IT-%'");
        }
        long variantId = scalar("SELECT variant_id FROM product_variants WHERE sku = 'HD-BSC-XL-DE'");
        System.out.println("Tồn kho ban đầu của HD-BSC-XL-DE (variant_id=" + variantId + "): " + scalar("SELECT stock_qty FROM product_variants WHERE variant_id=" + variantId));
        int n = 10;
        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger ok = new AtomicInteger(), rejected = new AtomicInteger();
        for (int i = 1; i <= n; i++) {
            final int k = i;
            pool.submit(() -> {
                try (Connection c = DriverManager.getConnection(URL, "root", "")) {
                    c.setAutoCommit(false);
                    go.await();
                    // Thứ tự giống OrderService.place(): 1) trừ tồn kho có điều kiện (khóa dòng biến thể) 2) tạo đơn 3) tạo dòng đơn
                    PreparedStatement dec = c.prepareStatement(
                            "UPDATE product_variants SET stock_qty = stock_qty - ?, version = version + 1 WHERE variant_id = ? AND stock_qty >= ?");
                    dec.setInt(1, 1); dec.setLong(2, variantId); dec.setInt(3, 1);
                    if (dec.executeUpdate() == 0) {
                        c.rollback(); rejected.incrementAndGet(); System.out.println("  Khách " + k + ": ROLLBACK -> OUT_OF_STOCK"); return null;
                    }
                    PreparedStatement o = c.prepareStatement("INSERT INTO orders(customer_id,order_code,receiver_name,receiver_phone,"
                            + "shipping_address,subtotal,shipping_fee,total,payment_method,status) VALUES (1,?,?,'0900','HN',459000,0,459000,'COD','CHO_XAC_NHAN')",
                            Statement.RETURN_GENERATED_KEYS);
                    o.setString(1, "AVQ-IT-" + k); o.setString(2, "Khách " + k); o.executeUpdate();
                    ResultSet rs = o.getGeneratedKeys(); rs.next(); long orderId = rs.getLong(1);
                    PreparedStatement li = c.prepareStatement("INSERT INTO order_items VALUES (?,?,'Hoodie Basic Nỉ Bông','XL','Đen',459000,1)");
                    li.setLong(1, orderId); li.setLong(2, variantId); li.executeUpdate();
                    c.commit(); ok.incrementAndGet(); System.out.println("  Khách " + k + ": COMMIT   -> tạo đơn AVQ-IT-" + k);
                } catch (Exception e) {
                    System.out.println("  Khách " + k + ": LỖI " + e.getMessage());
                }
                return null;
            });
        }
        go.countDown(); pool.shutdown(); pool.awaitTermination(30, TimeUnit.SECONDS);
        System.out.println("Kết quả: " + ok.get() + " đơn thành công, " + rejected.get() + " đơn bị từ chối");
        System.out.println("Tồn kho sau cùng: " + scalar("SELECT stock_qty FROM product_variants WHERE variant_id=" + variantId)
                + " | Số đơn AVQ-IT trong CSDL: " + scalar("SELECT COUNT(*) FROM orders WHERE order_code LIKE 'AVQ-IT-%'")
                + " | Số dòng đơn của biến thể: " + scalar("SELECT COUNT(*) FROM order_items oi JOIN orders o ON o.order_id = oi.order_id WHERE o.order_code LIKE 'AVQ-IT-%'"));
    }

    static long scalar(String sql) throws SQLException {
        try (Connection c = DriverManager.getConnection(URL, "root", ""); ResultSet rs = c.createStatement().executeQuery(sql)) { rs.next(); return rs.getLong(1); }
    }
}
