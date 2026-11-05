package vn.avq.shop.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

/** Adapter cổng VNPay (API 2.1.0): tạo URL thanh toán và xác thực chữ ký HMAC-SHA512. */
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "payment.provider", havingValue = "vnpay")
public class VnPayClient implements PaymentGatewayClient {
    private final String tmnCode;
    private final String hashSecret;
    private final String payUrl;
    private final String returnUrl;

    public VnPayClient(@Value("${vnpay.tmn-code}") String tmnCode, @Value("${vnpay.hash-secret}") String hashSecret,
                       @Value("${vnpay.pay-url}") String payUrl, @Value("${vnpay.return-url}") String returnUrl) {
        this.tmnCode = tmnCode; this.hashSecret = hashSecret; this.payUrl = payUrl; this.returnUrl = returnUrl;
    }

    @Override
    public String createPaymentUrl(String orderCode, BigDecimal amount) {
        Map<String, String> p = new TreeMap<>();
        p.put("vnp_Version", "2.1.0");
        p.put("vnp_Command", "pay");
        p.put("vnp_TmnCode", tmnCode);
        p.put("vnp_Amount", amount.movePointRight(2).toBigInteger().toString());
        p.put("vnp_CurrCode", "VND");
        p.put("vnp_TxnRef", orderCode);
        p.put("vnp_OrderInfo", "Thanh toan don " + orderCode);
        p.put("vnp_ReturnUrl", returnUrl);
        p.put("vnp_Locale", "vn");
        String query = canonical(p);
        return payUrl + "?" + query + "&vnp_SecureHash=" + hmacSha512(hashSecret, query);
    }

    @Override
    public boolean verifySignature(Map<String, String> params) {
        String received = params.get("vnp_SecureHash");
        if (received == null) return false;
        Map<String, String> data = new TreeMap<>(params);
        data.remove("vnp_SecureHash");
        data.remove("vnp_SecureHashType");
        String expected = hmacSha512(hashSecret, canonical(data));
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                received.toLowerCase().getBytes(StandardCharsets.US_ASCII));
    }

    public static String canonical(Map<String, String> sorted) {
        return sorted.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.US_ASCII))
                .collect(Collectors.joining("&"));
    }

    public static String hmacSha512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
