package vn.avq.shop.dto;

import java.math.BigDecimal;

public record UpdateProductRequest(BigDecimal price, String description, String material, Boolean visible) {}
