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
    private final ProductVariantRepository variantRepo;

    public ProductService(ProductRepository productRepo, CategoryRepository categoryRepo, SizeChartRepository chartRepo,
                          ProductVariantRepository variantRepo) {
        this.productRepo = productRepo; this.categoryRepo = categoryRepo; this.chartRepo = chartRepo; this.variantRepo = variantRepo;
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
        validateNew(r);
        for (CreateProductRequest.NewVariant v : r.variants())
            if (variantRepo.existsBySku(v.sku().trim()))
                throw new BusinessException("DUPLICATE", "Mã SKU \"" + v.sku().trim() + "\" đã được dùng cho sản phẩm khác");
        Category c = categoryRepo.findById(r.categoryId()).orElseThrow(() -> new BusinessException("NOT_FOUND", "Danh mục không tồn tại"));
        Product p = new Product(c, r.name(), r.price(), r.fitType() == null ? FitType.REGULAR : FitType.valueOf(r.fitType()));
        p.updateInfo(r.price(), r.description(), r.material());
        for (CreateProductRequest.NewVariant v : r.variants()) p.addVariant(v.size().trim(), v.color().trim(), v.sku().trim(), v.stock());
        if (!blank(r.imageUrl())) p.addImage(r.imageUrl(), true);
        return productRepo.save(p).getId();
    }

    /** Kiểm tra dữ liệu UC15 trước khi ghi CSDL, báo lỗi rõ ràng thay vì để MySQL từ chối (sku UNIQUE, uq_variant). */
    static void validateNew(CreateProductRequest r) {
        if (r.categoryId() == null) throw new BusinessException("VALIDATION_ERROR", "Vui lòng chọn danh mục");
        if (blank(r.name())) throw new BusinessException("VALIDATION_ERROR", "Vui lòng nhập tên sản phẩm");
        if (r.price() == null || r.price().signum() <= 0) throw new BusinessException("VALIDATION_ERROR", "Giá phải lớn hơn 0");
        if (r.variants() == null || r.variants().isEmpty())
            throw new BusinessException("VALIDATION_ERROR", "Sản phẩm phải có ít nhất một biến thể (size, màu)");
        Set<String> skus = new HashSet<>(), sizeColors = new HashSet<>();
        int i = 0;
        for (CreateProductRequest.NewVariant v : r.variants()) {
            String row = "Biến thể " + (++i) + ": ";
            if (blank(v.size()) || blank(v.color())) throw new BusinessException("VALIDATION_ERROR", row + "phải nhập size và màu");
            if (blank(v.sku())) throw new BusinessException("VALIDATION_ERROR", row + "phải nhập mã SKU");
            if (v.size().trim().length() > 5 || v.color().trim().length() > 30 || v.sku().trim().length() > 40)
                throw new BusinessException("VALIDATION_ERROR", row + "size tối đa 5, màu tối đa 30, SKU tối đa 40 ký tự");
            if (v.stock() < 0) throw new BusinessException("VALIDATION_ERROR", row + "tồn kho không được âm");
            if (!skus.add(v.sku().trim().toLowerCase()))
                throw new BusinessException("DUPLICATE", row + "mã SKU \"" + v.sku().trim() + "\" bị trùng với biến thể khác");
            if (!sizeColors.add(v.size().trim().toLowerCase() + "|" + v.color().trim().toLowerCase()))
                throw new BusinessException("DUPLICATE", row + "đã có biến thể size " + v.size().trim() + " màu " + v.color().trim());
        }
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
