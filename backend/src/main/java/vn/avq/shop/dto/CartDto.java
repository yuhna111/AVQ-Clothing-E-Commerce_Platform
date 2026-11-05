package vn.avq.shop.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartDto(int totalQuantity, BigDecimal subtotal, List<CartLineDto> items) {
    public CartDto(int totalQuantity, BigDecimal subtotal) { this(totalQuantity, subtotal, List.of()); }
}
