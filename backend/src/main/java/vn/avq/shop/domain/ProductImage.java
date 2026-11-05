package vn.avq.shop.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "product_images")
public class ProductImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id") private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id") private Product product;
    private String url;
    @Column(name = "is_main") private boolean main;
    @Column(name = "sort_order") private int sortOrder;

    protected ProductImage() {}
    ProductImage(Product product, String url, boolean main, int sortOrder) {
        this.product = product; this.url = url; this.main = main; this.sortOrder = sortOrder;
    }
    public String getUrl() { return url; }
    public boolean isMain() { return main; }
}
