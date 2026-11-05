package vn.avq.shop.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_histories")
public class OrderStatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id") private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id") private Order order;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "staff_id") private Staff staff;
    @Column(name = "from_status") private String fromStatus;
    @Column(name = "to_status") private String toStatus;
    private String note;
    @Column(name = "changed_at") private LocalDateTime changedAt;

    protected OrderStatusHistory() {}
    public static OrderStatusHistory of(Order o, OrderStatus from, OrderStatus to, Staff staff, String note, LocalDateTime now) {
        OrderStatusHistory h = new OrderStatusHistory();
        h.order = o; h.fromStatus = from == null ? null : from.name(); h.toStatus = to.name();
        h.staff = staff; h.note = note; h.changedAt = now;
        return h;
    }
    public Order getOrder() { return order; }
    public String getFromStatus() { return fromStatus; }
    public String getToStatus() { return toStatus; }
    public String getNote() { return note; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public String getStaffName() { return staff == null ? null : staff.getFullName(); }
}
