package vn.avq.shop.dto;

import java.math.BigDecimal;

public record OrderLineDto(String productName, String size, String color, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {}
