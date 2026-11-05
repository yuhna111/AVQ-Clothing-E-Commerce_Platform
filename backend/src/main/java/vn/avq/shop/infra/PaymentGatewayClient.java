package vn.avq.shop.infra;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentGatewayClient {
    String createPaymentUrl(String orderCode, BigDecimal amount);
    boolean verifySignature(Map<String, String> params);
}
