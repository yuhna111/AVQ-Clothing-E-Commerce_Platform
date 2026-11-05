package vn.avq.shop.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @EmbeddedId private OrderItemId id = new OrderItemId();
    @MapsId("orderId") @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id") private Order order;
    @MapsId("variantId") @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "variant_id") private ProductVariant variant;
    @Column(name = "product_name") private String productName;
    private String size;
    private String color;
    @Column(name = "unit_price") private BigDecimal unitPrice;
    private int quantity;

    protected OrderItem() {}
    OrderItem(Order order, ProductVariant v, int qty) {
        this.order = order; this.variant = v; this.quantity = qty;
        this.productName = v.getProduct().getName(); this.size = v.getSize(); this.color = v.getColor();
        this.unitPrice = v.getProduct().getPrice();
    }
    public BigDecimal lineTotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
    public ProductVariant getVariant() { return variant; }
    public int getQuantity() { return quantity; }
    public String getProductName() { return productName; }
    public String getSize() { return size; }
    public String getColor() { return color; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}
