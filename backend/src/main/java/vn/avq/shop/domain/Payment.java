package vn.avq.shop.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id") private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id") private Order order;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING) private PaymentStatus status;
    @Column(name = "txn_ref") private String txnRef;
    @Column(name = "paid_at") private LocalDateTime paidAt;

    protected Payment() {}
    public static Payment pending(Order order) {
        Payment p = new Payment(); p.order = order; p.amount = order.getTotal(); p.status = PaymentStatus.DANG_XU_LY; return p;
    }
    public void markResult(boolean success, String txnRef, LocalDateTime now) {
        this.status = success ? PaymentStatus.THANH_CONG : PaymentStatus.THAT_BAI;
        this.txnRef = txnRef;
        if (success) this.paidAt = now;
    }
    public void markMismatch(String txnRef) { this.status = PaymentStatus.SAI_LECH; this.txnRef = txnRef; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Order getOrder() { return order; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getTxnRef() { return txnRef; }
}
