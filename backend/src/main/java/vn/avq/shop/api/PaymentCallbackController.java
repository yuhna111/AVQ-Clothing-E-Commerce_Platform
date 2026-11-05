package vn.avq.shop.api;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.*;
import vn.avq.shop.common.BusinessException;
import vn.avq.shop.dto.*;
import vn.avq.shop.infra.MockPaymentGatewayClient;
import vn.avq.shop.repository.OrderRepository;
import vn.avq.shop.service.PaymentService;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentCallbackController {
    private final PaymentService paymentService;
    private final OrderRepository orderRepo;
    private final ObjectProvider<MockPaymentGatewayClient> mock;

    public PaymentCallbackController(PaymentService paymentService, OrderRepository orderRepo, ObjectProvider<MockPaymentGatewayClient> mock) {
        this.paymentService = paymentService; this.orderRepo = orderRepo; this.mock = mock;
    }

    /** IPN thật từ VNPay. */
    @GetMapping("/vnpay/ipn")
    public IpnResponse vnpayIpn(@RequestParam Map<String, String> params) { return paymentService.handleCallback(params); }

    /** Trang giả lập cổng thanh toán (chỉ bật khi payment.provider=mock): gửi IPN đã ký vào đúng luồng xử lý thật. */
    @PostMapping("/mock/{code}")
    public MockPayResult mockPay(@PathVariable String code, @RequestParam boolean success) {
        MockPaymentGatewayClient gw = mock.getIfAvailable();
        if (gw == null) throw new BusinessException("FORBIDDEN", "Cổng giả lập chỉ dùng khi payment.provider=mock");
        var order = orderRepo.findByOrderCode(code).orElseThrow(() -> new BusinessException("NOT_FOUND"));
        paymentService.ensurePending(order);
        IpnResponse r = paymentService.handleCallback(gw.signedIpn(code, order.getTotal(), success));
        String status = orderRepo.findByOrderCode(code).orElseThrow().getStatus().name();
        return new MockPayResult(r.RspCode(), r.Message(), status);
    }
}
