package vn.avq.shop.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "addresses")
public class Address {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id") private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "customer_id") private Customer customer;
    @Column(name = "receiver_name") private String receiverName;
    @Column(name = "receiver_phone") private String receiverPhone;
    @Column(name = "address_line") private String addressLine;
    @Column(name = "is_default") private boolean isDefault;

    protected Address() {}
    public Address(String receiverName, String receiverPhone, String addressLine) {
        this.receiverName = receiverName; this.receiverPhone = receiverPhone; this.addressLine = addressLine;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    void setCustomer(Customer c) { this.customer = c; }
    public String getReceiverName() { return receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public String getAddressLine() { return addressLine; }
}
