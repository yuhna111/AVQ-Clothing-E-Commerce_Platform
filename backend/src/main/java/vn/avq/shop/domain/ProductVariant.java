package vn.avq.shop.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "product_variants")
public class ProductVariant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id") private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id") private Product product;
    private String sku;
    private String size;
    private String color;
    @Column(name = "stock_qty") private int stockQty;
    @Version private long version;

    protected ProductVariant() {}
    ProductVariant(Product product, String sku, String size, String color, int stockQty) {
        this.product = product; this.sku = sku; this.size = size; this.color = color; setStock(stockQty);
    }
    public boolean isAvailable(int qty) { return product.isVisible() && stockQty >= qty; }
    public void setStock(int qty) {
        if (qty < 0) throw new IllegalArgumentException("Tồn kho phải là số nguyên không âm");      // BR06
        this.stockQty = qty;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Product getProduct() { return product; }
    public String getSku() { return sku; }
    public String getSize() { return size; }
    public String getColor() { return color; }
    public int getStockQty() { return stockQty; }
    public long getVersion() { return version; }
}
