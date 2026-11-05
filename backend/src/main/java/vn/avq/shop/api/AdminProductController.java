package vn.avq.shop.api;

import org.springframework.web.bind.annotation.*;
import vn.avq.shop.dto.*;
import vn.avq.shop.service.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminProductController {
    private final ProductService productService;
    private final StockService stockService;
    public AdminProductController(ProductService productService, StockService stockService) {
        this.productService = productService; this.stockService = stockService;
    }

    @GetMapping("/products")
    public List<ProductDetailDto> list() { return productService.adminList(); }

    @PostMapping("/products")
    public ProductDetailDto create(@RequestBody CreateProductRequest req) {
        return productService.detail(productService.create(req), true);
    }

    @PutMapping("/products/{id}")
    public ProductDetailDto update(@PathVariable Long id, @RequestBody UpdateProductRequest req) { return productService.update(id, req); }

    /** UC17: cập nhật tồn kho một biến thể; ghi nhật ký giá trị cũ/mới. */
    @PatchMapping("/variants/{variantId}/stock")
    public void updateStock(@RequestAttribute("uid") Long staffId, @PathVariable Long variantId, @RequestBody StockChange req) {
        stockService.updateStock(staffId, variantId, req.stock());
    }
}
