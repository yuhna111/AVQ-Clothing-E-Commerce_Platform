package vn.avq.shop.service;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/** DT-01 / BR09: phí 30.000 đ, miễn phí khi tạm tính từ 500.000 đ. */
@Component
public class ShippingFeeCalculator {
    static final BigDecimal FLAT_FEE = BigDecimal.valueOf(30_000);
    static final BigDecimal FREE_THRESHOLD = BigDecimal.valueOf(500_000);

    public BigDecimal calculate(BigDecimal subtotal) {
        return subtotal.compareTo(FREE_THRESHOLD) >= 0 ? BigDecimal.ZERO : FLAT_FEE;
    }
}
