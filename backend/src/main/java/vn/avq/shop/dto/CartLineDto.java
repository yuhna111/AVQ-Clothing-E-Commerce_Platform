package vn.avq.shop.dto;

import java.math.BigDecimal;

public record CartLineDto(Long variantId, Long productId, String productName, String size, String color, String image,
                          BigDecimal unitPrice, int quantity, int stock, BigDecimal lineTotal) {}
