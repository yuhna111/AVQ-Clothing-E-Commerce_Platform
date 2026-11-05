package vn.avq.shop.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("DT-01 Phí giao hàng (BR09)")
class ShippingFeeCalculatorTest {
    @ParameterizedTest(name = "tạm tính {0} đ -> phí {1} đ")
    @CsvSource({"249000,30000", "499999,30000", "500000,0", "708000,0"})
    void fee(long subtotal, long expected) {
        assertEquals(BigDecimal.valueOf(expected), new ShippingFeeCalculator().calculate(BigDecimal.valueOf(subtotal)));
    }
}
