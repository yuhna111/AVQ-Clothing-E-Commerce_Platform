package vn.avq.shop.dto;

import java.math.BigDecimal;

public record OrderSummaryDto(BigDecimal subtotal, BigDecimal shippingFee, BigDecimal total) {}
