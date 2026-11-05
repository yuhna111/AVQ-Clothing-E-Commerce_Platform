package vn.avq.shop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OrderItemId implements Serializable {
    @Column(name = "order_\1d") private Long orderId;
    @Column(name = "variant_id") private Long variantId;

    public OrderItemId() {}
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long v) { orderId = v; }
    public Long getVariantId() { return variantId; }
    public void setVariantId(Long v) { variantId = v; }
    @Override public boolean equals(Object o) {
        return o instanceof OrderItemId k && Objects.equals(orderId, k.orderId) && Objects.equals(variantId, k.variantId);
    }
    @Override public int hashCode() { return Objects.hash(orderId, variantId); }
}
