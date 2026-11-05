package vn.avq.shop.domain;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class User {
    public static final int MAX_FAILED = 5;                       // BR16
    public static final Duration LOCK_TIME = Duration.ofMinutes(15);

    @Column(name = "full_name", nullable = false) protected String fullName;
    @Column(name = "password_hash", nullable = false) protected String passwordHash;
    @Column(name = "failed_login_count") protected int failedLoginCount;
    @Column(name = "locked_until") protected LocalDateTime lockedUntil;

    public abstract Long getId();

    public boolean isLocked(LocalDateTime now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }
    /** Tăng bộ đếm sai; khóa 15 phút khi sai đủ 5 lần liên tiếp (BR16). */
    public void recordFailedLogin(LocalDateTime now) {
        failedLoginCount++;
        if (failedLoginCount >= MAX_FAILED) { lockedUntil = now.plus(LOCK_TIME); failedLoginCount = 0; }
    }
    public void resetFailedCount() { failedLoginCount = 0; lockedUntil = null; }

    public String getFullName() { return fullName; }
    public String getPasswordHash() { return passwordHash; }
    public int getFailedLoginCount() { return failedLoginCount; }
    public LocalDateTime getLockedUntil() { return lockedUntil; }
}
