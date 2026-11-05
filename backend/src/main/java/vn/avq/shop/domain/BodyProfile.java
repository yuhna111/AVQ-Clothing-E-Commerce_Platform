package vn.avq.shop.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "body_profiles")
public class BodyProfile {
    @Id @Column(name = "customer_id") private Long customerId;
    @Column(name = "height_cm") private int heightCm;
    @Column(name = "weight_kg") private int weightKg;
    @Enumerated(EnumType.STRING) @Column(name = "fit_pref") private FitPref fitPref;
    @Column(name = "consent_at") private LocalDateTime consentAt;

    protected BodyProfile() {}
    public BodyProfile(Long customerId, LocalDateTime consentAt) { this.customerId = customerId; this.consentAt = consentAt; }
    public void update(int heightCm, int weightKg, FitPref fitPref) {
        this.heightCm = heightCm; this.weightKg = weightKg; this.fitPref = fitPref;
    }
    public Long getCustomerId() { return customerId; }
    public int getHeightCm() { return heightCm; }
    public int getWeightKg() { return weightKg; }
    public FitPref getFitPref() { return fitPref; }
}
