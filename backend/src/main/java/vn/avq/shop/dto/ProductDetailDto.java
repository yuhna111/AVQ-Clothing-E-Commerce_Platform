package vn.avq.shop.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductDetailDto(Long id, String name, String category, String description, String material, String fitType,
                               BigDecimal price, boolean visible, List<String> images, List<VariantDto> variants, boolean hasSizeChart) {}
