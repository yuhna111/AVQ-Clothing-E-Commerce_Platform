package vn.avq.shop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderDto(String orderCode, LocalDateTime orderDate, String status, String paymentMethod, BigDecimal total,
                       int itemCount, String receiverName) {}
