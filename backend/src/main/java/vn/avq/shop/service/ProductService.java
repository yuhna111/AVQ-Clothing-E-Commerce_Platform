package vn.avq.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.avq.shop.common.BusinessException;
import vn.avq.shop.domain.*;
import vn.avq.shop.dto.*;
import vn.avq.shop.repository.*;
import java.util.*;

/** UC03–UC06 (xem, tìm, lọc) và UC14–UC16 (quản trị sản phẩm). */
@Service
public class ProductService {
    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final SizeChartRepository chartRepo;

    public ProductService(ProductRepository productRepo, CategoryRepository categoryRepo, SizeChartRepository chartRepo) {
        this.productRepo = productRepo; this.categoryRepo = categoryRepo; this.chartRepo = chartRepo;
    }

    @Transactional(readOnly = true)
    public List<ProductCardDto> search(ProductFilter f) {
        String q = blank(f.q()) ? null : f.q().trim();
        List<Product> found = productRepo.search(q, f.categoryId(), blank(f.size()) ? null : f.size(),
                blank(f.color()) ? null : f.color(), f.minPrice(), f.maxPrice());
        List<ProductCardDto> cards = new ArrayList<>(found.stream().map(ProductService::card).toList());
        Comparator<ProductCardDto> cmp = switch (f.sort() == null ? "" : f.sort()) {
            case "price_asc" -> Comparator.comparing(ProductCardDto::price);
            case "price_desc" -> Comparator.comparing(ProductCardDto::price).reversed();
            default -> Comparator.comparing(ProductCardDto::id).reversed();                 // mới nhất
        };
        cards.sort(cmp);
        return cards;
    }

    @Transactional(readOnly = true)
    public ProductDetailDto detail(Long id, boolean includeHidden) {
        Product p = productRepo.findById(id).filter(x -> includeHidden || x.isVisible())
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Không tìm thấy sản phẩm"));
        return detailOf(p);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> categories() {
        return categoryRepo.findAll().stream().map(c -> new CategoryDto(c.getId(), c.getName(), c.getSlug())).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDetailDto> adminList() {
        return productRepo.findAll().stream().sorted(Comparator.comparing(Product::getId)).map(this::detailOf).toList();
    }

    /** UC15: thêm sản phẩm kèm ít nhất một biến thể (BR17). */
    @Transactional
    public Long create(CreateProductRequest r) {
        if (r.variants() == null || r.variants().isEmpty())
            throw new BusinessException("VALIDATION_ERROR", "Sản phẩm phải có ít nhất một biến thể (size, màu)");
        Category c = categoryRepo.findById(r.categoryId()).orElseThrow(() -> new BusinessException("NOT_FOUND", "Danh mục không tồn tại"));
        Product p = new Product(c, r.name(), r.price(), r.fitType() == null ? FitType.REGULAR : FitType.valueOf(r.fitType()));
        p.updateInfo(r.price(), r.description(), r.material());
        for (CreateProductRequest.NewVariant v : r.variants()) p.addVariant(v.size(), v.color(), v.sku(), v.stock());
        if (!blank(r.imageUrl())) p.addImage(r.imageUrl(), true);
        return productRepo.save(p).getId();
    }

    /** UC16: sửa giá, mô tả, ẩn/hiện. */
    @Transactional
    public ProductDetailDto update(Long id, UpdateProductRequest r) {
        Product p = productRepo.findById(id).orElseThrow(() -> new BusinessException("NOT_FOUND"));
        p.updateInfo(r.price() == null ? p.getPrice() : r.price(),
                r.description() == null ? p.getDescription() : r.description(),
                r.material() == null ? p.getMaterial() : r.material());
        if (r.visible() != null) p.setVisible(r.visible());
        return detailOf(productRepo.save(p));
    }

    private ProductDetailDto detailOf(Product p) {
        return new ProductDetailDto(p.getId(), p.getName(), p.getCategory().getName(), p.getDescription(), p.getMaterial(),
                p.getFitType().name(), p.getPrice(), p.isVisible(), p.getImages().stream().map(ProductImage::getUrl).toList(),
                p.getVariants().stream().map(v -> new VariantDto(v.getId(), v.getSku(), v.getSize(), v.getColor(), v.getStockQty())).toList(),
                !chartRepo.findByProductIdOrderByHeightMin(p.getId()).isEmpty());
    }

    static ProductCardDto card(Product p) {
        List<String> order = List.of("S", "M", "L", "XL", "XXL");
        List<String> sizes = p.getVariants().stream().filter(v -> v.getStockQty() > 0).map(ProductVariant::getSize).distinct()
                .sorted(Comparator.comparingInt(s -> order.indexOf(s) < 0 ? 99 : order.indexOf(s))).toList();
        List<String> colors = p.getVariants().stream().map(ProductVariant::getColor).distinct().toList();
        int stock = p.getVariants().stream().mapToInt(ProductVariant::getStockQty).sum();
        return new ProductCardDto(p.getId(), p.getName(), p.getCategory().getName(), p.getPrice(), p.mainImage(), sizes, colors, stock);
    }

    private static boolean blank(String s) { return s == null || s.isBlank(); }
}
