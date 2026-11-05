package vn.avq.shop.api;

import org.springframework.web.bind.annotation.*;
import vn.avq.shop.dto.*;
import vn.avq.shop.service.ProductService;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService productService) { this.productService = productService; }

    @GetMapping("/products")
    public List<ProductCardDto> search(@RequestParam(required = false) String q, @RequestParam(required = false) Long categoryId,
                                       @RequestParam(required = false) String size, @RequestParam(required = false) String color,
                                       @RequestParam(required = false) BigDecimal minPrice, @RequestParam(required = false) BigDecimal maxPrice,
                                       @RequestParam(required = false) String sort) {
        return productService.search(new ProductFilter(q, categoryId, size, color, minPrice, maxPrice, sort));
    }

    @GetMapping("/products/{id}")
    public ProductDetailDto detail(@PathVariable Long id) { return productService.detail(id, false); }

    @GetMapping("/categories")
    public List<CategoryDto> categories() { return productService.categories(); }
}
