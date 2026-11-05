package vn.avq.shop.service;

import org.junit.jupiter.api.*;
import vn.avq.shop.domain.*;
import vn.avq.shop.dto.*;
import vn.avq.shop.infra.VnPayClient;
import vn.avq.shop.support.Shop;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UC10 Pay Online — xử lý IPN VNPay (DT-05)")
class PaymentServiceTest {
    Shop s; OrderResultDto order;
    @BeforeEach void setUp() {
        s = new Shop();
        s.cartService.addToCart(s.khachA.getId(), s.aoThunM.getId(), 1);
        order = s.orderService.place(s.khachA.getId(), new PlaceOrderRequest(12L, PaymentMethod.ONLINE, null));
    }
    /** Giả lập IPN do VNPay gửi, ký bằng cùng hash secret. */
    Map<String, String> ipn(String amountX100, String responseCode, String secret) {
        Map<String, String> p = new TreeMap<>();
        p.put("vnp_Amount", amountX100); p.put("vnp_ResponseCode", responseCode); p.put("vnp_TmnCode", "AVQTEST1");
        p.put("vnp_TransactionNo", "14512345"); p.put("vnp_TxnRef", order.orderCode());
        p.put("vnp_SecureHash", VnPayClient.hmacSha512(secret, VnPayClient.canonical(p)));
        return p;
    }
    @Test @DisplayName("IPN hợp lệ, thành công -> RspCode 00, đơn sang Chờ xác nhận")
    void success() {
        assertEquals("00", s.paymentService.handleCallback(ipn("27900000", "00", "SECRETKEY-SANDBOX")).RspCode());
        assertEquals(OrderStatus.CHO_XAC_NHAN, s.orders.findByOrderCode(order.orderCode()).orElseThrow().getStatus());
        assertEquals(PaymentStatus.THANH_CONG, s.payments.findAll().get(0).getStatus());
    }
    @Test @DisplayName("IPN gửi lặp -> RspCode 02, không xử lý lần hai (NFR11)")
    void duplicate() {
        Map<String, String> p = ipn("27900000", "00", "SECRETKEY-SANDBOX");
        s.paymentService.handleCallback(p);
        assertEquals("02", s.paymentService.handleCallback(p).RspCode());
        assertEquals(2, s.histories.items.size());
    }
    @Test @DisplayName("Chữ ký sai -> RspCode 97, đơn giữ nguyên")
    void badSignature() {
        assertEquals("97", s.paymentService.handleCallback(ipn("27900000", "00", "KHOA-GIA-MAO")).RspCode());
        assertEquals(OrderStatus.CHO_THANH_TOAN, s.orders.findByOrderCode(order.orderCode()).orElseThrow().getStatus());
    }
    @Test @DisplayName("Số tiền không khớp -> RspCode 04, Payment SAI_LECH")
    void amountMismatch() {
        assertEquals("04", s.paymentService.handleCallback(ipn("100000", "00", "SECRETKEY-SANDBOX")).RspCode());
        assertEquals(PaymentStatus.SAI_LECH, s.payments.findAll().get(0).getStatus());
    }
    @Test @DisplayName("Thanh toán thất bại -> đơn giữ Chờ thanh toán (EX-03)")
    void failed() {
        assertEquals("00", s.paymentService.handleCallback(ipn("27900000", "24", "SECRETKEY-SANDBOX")).RspCode());
        assertEquals(OrderStatus.CHO_THANH_TOAN, s.orders.findByOrderCode(order.orderCode()).orElseThrow().getStatus());
        assertEquals(PaymentStatus.THAT_BAI, s.payments.findAll().get(0).getStatus());
    }
}
