package vn.avq.shop.dto;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductRequest(Long categoryId, String name, String description, String material, String fitType,
                                   BigDecimal price, String imageUrl, List<NewVariant> variants) {
    public record NewVariant(String size, String color, String sku, int stock) {}
}
