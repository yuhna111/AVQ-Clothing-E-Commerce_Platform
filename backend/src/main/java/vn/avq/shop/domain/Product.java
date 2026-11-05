package vn.avq.shop.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id") private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id") private Category category;
    private String name;
    private String description;
    private String material;
    @Enumerated(EnumType.STRING) @Column(name = "fit_type") private FitType fitType = FitType.REGULAR;
    private BigDecimal price;
    @Column(name = "is_visible") private boolean visible = true;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>();
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    protected Product() {}
    public Product(Category category, String name, BigDecimal price, FitType fitType) {
        this.category = category; this.name = name; this.fitType = fitType; setPrice(price);
    }
    /** BR17: mỗi sản phẩm có ít nhất một biến thể; SKU duy nhất do CSDL bảo đảm. */
    public ProductVariant addVariant(String size, String color, String sku, int stock) {
        ProductVariant v = new ProductVariant(this, sku, size, color, stock);
        variants.add(v);
        return v;
    }
    public void updateInfo(BigDecimal price, String description, String material) {
        setPrice(price); this.description = description; this.material = material;
    }
    private void setPrice(BigDecimal p) {
        if (p == null || p.signum() <= 0) throw new IllegalArgumentException("Giá phải lớn hơn 0");   // BR06
        this.price = p;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public FitType getFitType() { return fitType; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public List<ProductVariant> getVariants() { return variants; }
    public Category getCategory() { return category; }
    public String getDescription() { return description; }
    public String getMaterial() { return material; }
    public List<ProductImage> getImages() { return images; }
    public void addImage(String url, boolean main) { images.add(new ProductImage(this, url, main, images.size())); }
    public String mainImage() { return images.stream().filter(ProductImage::isMain).map(ProductImage::getUrl).findFirst()
            .orElse(images.isEmpty() ? null : images.get(0).getUrl()); }
}
