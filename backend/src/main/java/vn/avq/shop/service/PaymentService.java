package vn.avq.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.avq.shop.domain.*;
import vn.avq.shop.dto.*;
import vn.avq.shop.infra.PaymentGatewayClient;
import vn.avq.shop.repository.*;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class PaymentService {
    private final PaymentGatewayClient gateway;
    private final PaymentRepository paymentRepo;
    private final OrderRepository orderRepo;
    private final OrderStateMachine stateMachine;
    private final Clock clock;

    public PaymentService(PaymentGatewayClient gateway, PaymentRepository paymentRepo, OrderRepository orderRepo,
                          OrderStateMachine stateMachine, Clock clock) {
        this.gateway = gateway; this.paymentRepo = paymentRepo; this.orderRepo = orderRepo;
        this.stateMachine = stateMachine; this.clock = clock;
    }

    public String createPayment(Order order) {
        paymentRepo.save(Payment.pending(order));
        return gateway.createPaymentUrl(order.getOrderCode(), order.getTotal());
    }

    /** Thanh toán lại (EX-03): bảo đảm đơn "Chờ thanh toán" có một Payment đang xử lý. */
    @Transactional
    public void ensurePending(Order order) {
        if (order.getStatus() == OrderStatus.CHO_THANH_TOAN
                && paymentRepo.findFirstByOrderIdAndStatus(order.getId(), PaymentStatus.DANG_XU_LY).isEmpty())
            paymentRepo.save(Payment.pending(order));
    }

    /** UC10, DT-05: xử lý IPN đúng một lần, xác thực chữ ký và số tiền (NFR11). */
    @Transactional
    public IpnResponse handleCallback(Map<String, String> params) {
        if (!gateway.verifySignature(params)) return IpnResponse.INVALID_SIGNATURE;
        String txnRef = params.get("vnp_TransactionNo");
        if (paymentRepo.existsByTxnRef(txnRef)) return IpnResponse.ALREADY_CONFIRMED;
        Order order = orderRepo.findByOrderCode(params.get("vnp_TxnRef")).orElse(null);
        if (order == null) return IpnResponse.ORDER_NOT_FOUND;
        Payment payment = paymentRepo.findFirstByOrderIdAndStatus(order.getId(), PaymentStatus.DANG_XU_LY).orElse(null);
        if (payment == null) return IpnResponse.ALREADY_CONFIRMED;
        BigDecimal amount = new BigDecimal(params.get("vnp_Amount")).movePointLeft(2);   // VNPay gửi số tiền x100
        if (amount.compareTo(order.getTotal()) != 0) {
            payment.markMismatch(txnRef);
            paymentRepo.save(payment);
            return IpnResponse.INVALID_AMOUNT;
        }
        boolean success = "00".equals(params.get("vnp_ResponseCode"));
        payment.markResult(success, txnRef, LocalDateTime.now(clock));
        paymentRepo.save(payment);
        if (success && order.getStatus() == OrderStatus.CHO_THANH_TOAN) {
            stateMachine.transit(order, OrderStatus.CHO_XAC_NHAN, Actor.SYSTEM, "Thanh toán thành công");
            orderRepo.save(order);
        }
        return IpnResponse.OK;
    }
}
