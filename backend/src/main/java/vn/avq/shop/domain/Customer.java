package vn.avq.shop.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
public class Customer extends User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id") private Long id;
    private String email;
    private String phone;
    @Column(name = "created_at") private LocalDateTime createdAt = LocalDateTime.now();
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    protected Customer() {}
    public Customer(String fullName, String email, String phone, String passwordHash) {
        this.fullName = fullName; this.email = email; this.phone = phone; this.passwordHash = passwordHash;
    }
    public void addAddress(Address a) { a.setCustomer(this); addresses.add(a); }

    @Override public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public List<Address> getAddresses() { return addresses; }
}
