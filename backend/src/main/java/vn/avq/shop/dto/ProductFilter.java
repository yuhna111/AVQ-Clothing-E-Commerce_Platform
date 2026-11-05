package vn.avq.shop.dto;

import java.math.BigDecimal;

public record ProductFilter(String q, Long categoryId, String size, String color, BigDecimal minPrice, BigDecimal maxPrice, String sort) {}
