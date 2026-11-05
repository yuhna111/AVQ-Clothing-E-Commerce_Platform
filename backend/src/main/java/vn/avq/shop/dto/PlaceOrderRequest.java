package vn.avq.shop.dto;

import java.math.BigDecimal;
import vn.avq.shop.domain.PaymentMethod;

public record PlaceOrderRequest(Long addressId, PaymentMethod paymentMethod, BigDecimal expectedTotal) {}
