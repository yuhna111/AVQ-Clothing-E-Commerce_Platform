package vn.avq.shop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import vn.avq.shop.domain.Order;

public record OrderResultDto(String orderCode, String status, BigDecimal subtotal, BigDecimal shippingFee,
                             BigDecimal total, LocalDateTime expiresAt, String paymentUrl) {
    public static OrderResultDto from(Order o, String paymentUrl) {
        return new OrderResultDto(o.getOrderCode(), o.getStatus().name(), o.getSubtotal(), o.getShippingFee(),
                o.getTotal(), o.getExpiresAt(), paymentUrl);
    }
}
