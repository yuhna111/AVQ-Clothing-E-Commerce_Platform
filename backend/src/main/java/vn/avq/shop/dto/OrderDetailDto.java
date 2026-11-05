package vn.avq.shop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailDto(String orderCode, String status, String paymentMethod, LocalDateTime orderDate, LocalDateTime expiresAt,
                             String receiverName, String receiverPhone, String shippingAddress, String trackingCode,
                             BigDecimal subtotal, BigDecimal shippingFee, BigDecimal total, List<OrderLineDto> items,
                             List<HistoryDto> history, List<String> nextStatuses, boolean canCancel) {}
