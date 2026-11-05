package vn.avq.shop.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductCardDto(Long id, String name, String category, BigDecimal price, String image,
                             List<String> sizes, List<String> colors, int totalStock) {}
