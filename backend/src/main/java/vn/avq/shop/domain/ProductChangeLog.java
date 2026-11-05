package vn.avq.shop.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_change_logs")
public class ProductChangeLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id") private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id") private Product product;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "variant_id") private ProductVariant variant;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "staff_id") private Staff staff;
    @Column(name = "field_name") private String fieldName;
    @Column(name = "old_value") private String oldValue;
    @Column(name = "new_value") private String newValue;
    @Column(name = "changed_at") private LocalDateTime changedAt;

    protected ProductChangeLog() {}
    public static ProductChangeLog of(Staff staff, ProductVariant v, String field, String oldValue, String newValue, LocalDateTime now) {
        ProductChangeLog l = new ProductChangeLog();
        l.staff = staff; l.variant = v; l.product = v.getProduct(); l.fieldName = field;
        l.oldValue = oldValue; l.newValue = newValue; l.changedAt = now;
        return l;
    }
    public String getFieldName() { return fieldName; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
}
