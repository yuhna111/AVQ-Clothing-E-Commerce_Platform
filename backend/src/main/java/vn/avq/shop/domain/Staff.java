package vn.avq.shop.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "staff")
public class Staff extends User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id") private Long id;
    private String username;
    @Enumerated(EnumType.STRING) private StaffRole role = StaffRole.STAFF;
    @Column(name = "is_active") private boolean active = true;

    protected Staff() {}
    public Staff(String fullName, String username, String passwordHash) {
        this.fullName = fullName; this.username = username; this.passwordHash = passwordHash;
    }
    @Override public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public StaffRole getRole() { return role; }
    public boolean isActive() { return active; }
}
