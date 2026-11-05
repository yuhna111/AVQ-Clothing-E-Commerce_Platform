package vn.avq.shop.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "carts")
public class Cart {
    public static final int MAX_QTY_PER_LINE = 10;                 // BR05

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id") private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "customer_id") private Customer customer;
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
    @Column(name = "updated_at") private LocalDateTime updatedAt;

    protected Cart() {}
    public Cart(Customer customer) { this.customer = customer; this.updatedAt = LocalDateTime.now(); }

    public Optional<CartItem> findItem(Long variantId) {
        return items.stream().filter(i -> i.getVariant().getId().equals(variantId)).findFirst();
    }
    /** BR07: mỗi biến thể một dòng; thêm lại thì cộng dồn. Kiểm tra tồn kho do CartService thực hiện. */
    public CartItem addOrIncrease(ProductVariant variant, int qty) {
        CartItem item = findItem(variant.getId()).orElse(null);
        if (item == null) { item = new CartItem(this, variant, qty); items.add(item); }
        else item.changeQty(item.getQuantity() + qty);
        updatedAt = LocalDateTime.now();
        return item;
    }
    public void removeItems(Set<Long> variantIds) { items.removeIf(i -> variantIds.contains(i.getVariant().getId())); updatedAt = LocalDateTime.now(); }
    public BigDecimal subtotal() {
        return items.stream().map(CartItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public int totalQuantity() { return items.stream().mapToInt(CartItem::getQuantity).sum(); }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public List<CartItem> getItems() { return items; }
}
