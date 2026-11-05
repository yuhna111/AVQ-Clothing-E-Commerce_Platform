package vn.avq.shop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CartItemId implements Serializable {
    @Column(name = "cart_\1d") private Long cartId;
    @Column(name = "variant_id") private Long variantId;

    public CartItemId() {}
    public Long getCartId() { return cartId; }
    public void setCartId(Long v) { cartId = v; }
    public Long getVariantId() { return variantId; }
    public void setVariantId(Long v) { variantId = v; }
    @Override public boolean equals(Object o) {
        return o instanceof CartItemId k && Objects.equals(cartId, k.cartId) && Objects.equals(variantId, k.variantId);
    }
    @Override public int hashCode() { return Objects.hash(cartId, variantId); }
}
