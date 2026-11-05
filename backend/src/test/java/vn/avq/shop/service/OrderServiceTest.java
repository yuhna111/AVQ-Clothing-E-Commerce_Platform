package vn.avq.shop.service;

import org.junit.jupiter.api.*;
import vn.avq.shop.common.BusinessException;
import vn.avq.shop.domain.*;
import vn.avq.shop.dto.*;
import vn.avq.shop.support.Shop;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UC09 Place Order, UC12 Cancel, BR12 Expiry")
class OrderServiceTest {
    Shop s;
    @BeforeEach void setUp() { s = new Shop(); }
    Long cid() { return s.khachA.getId(); }

    @Test @DisplayName("COD 708.000 đ: miễn phí giao, Chờ xác nhận, trừ tồn, dọn giỏ (MS-04)")
    void placeCod() {
        s.cartService.addToCart(cid(), s.hoodieL.getId(), 1);
        s.cartService.addToCart(cid(), s.aoThunM.getId(), 1);
        OrderSummaryDto sum = s.orderService.prepare(cid());
        OrderResultDto r = s.orderService.place(cid(), new PlaceOrderRequest(12L, PaymentMethod.COD, sum.total()));
        assertEquals("CHO_XAC_NHAN", r.status());
        assertEquals(BigDecimal.ZERO, r.shippingFee());
        assertEquals(BigDecimal.valueOf(708_000), r.total());
        assertNull(r.paymentUrl());
        assertEquals(6, s.hoodieL.getStockQty());
        assertEquals(11, s.aoThunM.getStockQty());
        assertTrue(s.carts.findByCustomerId(cid()).orElseThrow().getItems().isEmpty());
        assertEquals("CHO_XAC_NHAN", s.histories.items.get(0).getToStatus());
    }
    @Test @DisplayName("Online 249.000 đ: phí 30.000 đ, Chờ thanh toán, hạn 15 phút, có URL VNPay (MS-05)")
    void placeOnline() {
        s.cartService.addToCart(cid(), s.aoThunM.getId(), 1);
        OrderResultDto r = s.orderService.place(cid(), new PlaceOrderRequest(12L, PaymentMethod.ONLINE, BigDecimal.valueOf(279_000)));
        assertEquals("CHO_THANH_TOAN", r.status());
        assertEquals(BigDecimal.valueOf(279_000), r.total());
        assertEquals(s.now().plusMinutes(15), r.expiresAt());
        assertTrue(r.paymentUrl().contains("vnp_Amount=27900000") && r.paymentUrl().contains("vnp_SecureHash="));
        assertEquals(PaymentStatus.DANG_XU_LY, s.payments.findAll().get(0).getStatus());
    }
    @Test @DisplayName("Tổng tiền khác với khách đã thấy -> PRICE_CHANGED (BR08)")
    void priceChanged() {
        s.cartService.addToCart(cid(), s.aoThunM.getId(), 1);
        BusinessException e = assertThrows(BusinessException.class,
                () -> s.orderService.place(cid(), new PlaceOrderRequest(12L, PaymentMethod.COD, BigDecimal.valueOf(249_000))));
        assertEquals("PRICE_CHANGED", e.getCode());
        assertEquals(12, s.aoThunM.getStockQty());
    }
    @Test @DisplayName("10 khách cùng mua chiếc XL cuối cùng -> chỉ 1 đơn thành công (NFR05)")
    void lastItemRace() throws Exception {
        int n = 10; Long[] ids = new Long[n];
        for (int i = 0; i < n; i++) {
            Customer c = s.customers.save(new Customer("Khách " + i, "k" + i + "@avq.vn", "09000000" + i, "{plain}x"));
            Address a = new Address("Khách " + i, "0900", "HN"); a.setId(100L + i); c.addAddress(a);
            ids[i] = c.getId();
            s.cartService.addToCart(c.getId(), s.hoodieXL.getId(), 1);
        }
        ExecutorService pool = Executors.newFixedThreadPool(n); CountDownLatch go = new CountDownLatch(1);
        AtomicInteger ok = new AtomicInteger(), outOfStock = new AtomicInteger();
        for (int i = 0; i < n; i++) { final int k = i; pool.submit(() -> { go.await();
            try { s.orderService.place(ids[k], new PlaceOrderRequest(100L + k, PaymentMethod.COD, null)); ok.incrementAndGet(); }
            catch (BusinessException e) { if (e.getCode().equals("OUT_OF_STOCK")) outOfStock.incrementAndGet(); }
            return null; }); }
        go.countDown(); pool.shutdown(); pool.awaitTermination(10, TimeUnit.SECONDS);
        assertEquals(1, ok.get()); assertEquals(9, outOfStock.get()); assertEquals(0, s.hoodieXL.getStockQty());
    }
    @Test @DisplayName("Khách hủy đơn Chờ xác nhận -> Đã hủy, hoàn tồn kho (UC12, BR11)")
    void cancelRestoresStock() {
        s.cartService.addToCart(cid(), s.hoodieL.getId(), 2);
        OrderResultDto r = s.orderService.place(cid(), new PlaceOrderRequest(12L, PaymentMethod.COD, null));
        assertEquals(5, s.hoodieL.getStockQty());
        s.orderService.cancel(cid(), r.orderCode(), "Đổi ý");
        assertEquals(OrderStatus.DA_HUY, s.orders.findByOrderCode(r.orderCode()).orElseThrow().getStatus());
        assertEquals(7, s.hoodieL.getStockQty());
    }
    @Test @DisplayName("Khách không hủy được đơn Đang giao -> INVALID_TRANSITION (BR13)")
    void cannotCancelShipping() {
        s.cartService.addToCart(cid(), s.hoodieL.getId(), 1);
        OrderResultDto r = s.orderService.place(cid(), new PlaceOrderRequest(12L, PaymentMethod.COD, null));
        s.orderService.updateStatus(s.nhanVien.getId(), r.orderCode(), OrderStatus.DANG_GIAO, "GHN-8F2K19");
        assertEquals("INVALID_TRANSITION", assertThrows(BusinessException.class, () -> s.orderService.cancel(cid(), r.orderCode(), "x")).getCode());
        assertEquals("GHN-8F2K19", s.orders.findByOrderCode(r.orderCode()).orElseThrow().getTrackingCode());
    }
    @Test @DisplayName("Nhân viên hủy không ghi lý do -> CANCEL_REASON_REQUIRED (UC19 2a)")
    void staffCancelNeedsReason() {
        s.cartService.addToCart(cid(), s.hoodieL.getId(), 1);
        OrderResultDto r = s.orderService.place(cid(), new PlaceOrderRequest(12L, PaymentMethod.COD, null));
        assertEquals("CANCEL_REASON_REQUIRED", assertThrows(BusinessException.class,
                () -> s.orderService.updateStatus(s.nhanVien.getId(), r.orderCode(), OrderStatus.DA_HUY, " ")).getCode());
    }
    @Test @DisplayName("Đơn online không thanh toán sau 15 phút -> scheduler hủy, hoàn tồn (BR12)")
    void expiry() {
        s.cartService.addToCart(cid(), s.hoodieL.getId(), 1);
        s.orderService.place(cid(), new PlaceOrderRequest(12L, PaymentMethod.ONLINE, null));
        s.clock.advance(Duration.ofMinutes(14));
        assertEquals(0, s.orderService.expirePendingOrders());
        s.clock.advance(Duration.ofMinutes(2));
        assertEquals(1, s.orderService.expirePendingOrders());
        assertEquals(7, s.hoodieL.getStockQty());
    }
}
