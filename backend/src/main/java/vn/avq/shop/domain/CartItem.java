package vn.avq.shop.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
public class CartItem {
    @EmbeddedId private CartItemId id = new CartItemId();
    @MapsId("cartId") @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cart_id") private Cart cart;
    @MapsId("variantId") @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "variant_id") private ProductVariant variant;
    private int quantity;
    @Column(name = "added_at") private LocalDateTime addedAt = LocalDateTime.now();

    protected CartItem() {}
    CartItem(Cart cart, ProductVariant variant, int qty) {
        this.cart = cart; this.variant = variant; changeQty(qty);
    }
    public void changeQty(int qty) {
        if (qty < 1 || qty > Cart.MAX_QTY_PER_LINE) throw new IllegalArgumentException("Số lượng mỗi dòng từ 1 đến 10");
        this.quantity = qty;
    }
    public BigDecimal lineTotal() { return variant.getProduct().getPrice().multiply(BigDecimal.valueOf(quantity)); }
    public ProductVariant getVariant() { return variant; }
    public int getQuantity() { return quantity; }
}
