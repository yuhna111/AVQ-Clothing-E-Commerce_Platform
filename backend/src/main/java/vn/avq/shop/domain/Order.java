package vn.avq.shop.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity(name = "CustomerOrder")
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id") private Long id;
    @Column(name = "order_code") private String orderCode;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "customer_id") private Customer customer;
    @Enumerated(EnumType.STRING) private OrderStatus status;
    @Enumerated(EnumType.STRING) @Column(name = "payment_method") private PaymentMethod paymentMethod;
    private BigDecimal subtotal = BigDecimal.ZERO;
    @Column(name = "shipping_fee") private BigDecimal shippingFee = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    @Column(name = "receiver_name") private String receiverName;
    @Column(name = "receiver_phone") private String receiverPhone;
    @Column(name = "shipping_address") private String shippingAddress;
    @Column(name = "order_date") private LocalDateTime orderDate;
    @Column(name = "expires_at") private LocalDateTime expiresAt;
    @Column(name = "tracking_code") private String trackingCode;
    @Version private long version;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {}
    /** Tạo đơn mới; lưu ảnh chụp địa chỉ nhận hàng tại thời điểm đặt. */
    public static Order newOrder(String orderCode, Customer customer, Address address, PaymentMethod method, LocalDateTime now) {
        Order o = new Order();
        o.orderCode = orderCode; o.customer = customer; o.paymentMethod = method; o.orderDate = now;
        o.receiverName = address.getReceiverName(); o.receiverPhone = address.getReceiverPhone();
        o.shippingAddress = address.getAddressLine();
        return o;
    }
    /** Chốt giá hiện hành của sản phẩm vào dòng đơn (BR08). */
    public void addItem(ProductVariant variant, int qty) {
        items.add(new OrderItem(this, variant, qty));
    }
    public void recalculate() {
        subtotal = items.stream().map(OrderItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        total = subtotal.add(shippingFee);
    }
    public Set<Long> getVariantIds() { return items.stream().map(i -> i.getVariant().getId()).collect(Collectors.toSet()); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderCode() { return orderCode; }
    public Customer getCustomer() { return customer; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus s) { this.status = s; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod m) { this.paymentMethod = m; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal fee) { this.shippingFee = fee; }
    public BigDecimal getTotal() { return total; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime t) { this.expiresAt = t; }
    public String getTrackingCode() { return trackingCode; }
    public void setTrackingCode(String c) { this.trackingCode = c; }
    public List<OrderItem> getItems() { return items; }
    public String getShippingAddress() { return shippingAddress; }
    public String getReceiverName() { return receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public LocalDateTime getOrderDate() { return orderDate; }
}
