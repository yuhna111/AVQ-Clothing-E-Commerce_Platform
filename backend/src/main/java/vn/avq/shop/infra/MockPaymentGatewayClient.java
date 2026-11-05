package vn.avq.shop.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.*;

/**
 * Cổng thanh toán giả lập cho demo (payment.provider=mock): tạo URL tới trang giả lập của frontend và
 * ký kết quả bằng cùng thuật toán HMAC-SHA512 như VNPay, nên PaymentService xử lý y hệt IPN thật.
 */
@Component
@ConditionalOnProperty(name = "payment.provider", havingValue = "mock", matchIfMissing = true)
public class MockPaymentGatewayClient implements PaymentGatewayClient {
    private final String hashSecret;
    public MockPaymentGatewayClient(@Value("${vnpay.hash-secret}") String hashSecret) { this.hashSecret = hashSecret; }

    @Override
    public String createPaymentUrl(String orderCode, BigDecimal amount) {
        return "/thanh-toan-gia-lap?order=" + orderCode + "&amount=" + amount.toBigInteger();
    }

    @Override
    public boolean verifySignature(Map<String, String> params) {
        Map<String, String> data = new TreeMap<>(params);
        String received = data.remove("vnp_SecureHash");
        return received != null && received.equals(VnPayClient.hmacSha512(hashSecret, VnPayClient.canonical(data)));
    }

    /** Tạo tham số IPN đã ký, giống dữ liệu VNPay gửi về sau khi khách thanh toán. */
    public Map<String, String> signedIpn(String orderCode, BigDecimal amount, boolean success) {
        Map<String, String> p = new TreeMap<>();
        p.put("vnp_Amount", amount.movePointRight(2).toBigInteger().toString());
        p.put("vnp_ResponseCode", success ? "00" : "24");
        p.put("vnp_TmnCode", "AVQDEMO");
        p.put("vnp_TransactionNo", String.valueOf(System.currentTimeMillis()));
        p.put("vnp_TxnRef", orderCode);
        p.put("vnp_SecureHash", VnPayClient.hmacSha512(hashSecret, VnPayClient.canonical(p)));
        return p;
    }
}
