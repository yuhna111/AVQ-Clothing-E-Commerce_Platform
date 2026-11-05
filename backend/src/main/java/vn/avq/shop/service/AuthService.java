package vn.avq.shop.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.avq.shop.common.BusinessException;
import vn.avq.shop.domain.*;
import vn.avq.shop.dto.*;
import vn.avq.shop.infra.JwtTokenProvider;
import vn.avq.shop.repository.*;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class AuthService {
    private final CustomerRepository customerRepo;
    private final StaffRepository staffRepo;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwt;
    private final Clock clock;

    public AuthService(CustomerRepository customerRepo, StaffRepository staffRepo, PasswordEncoder encoder,
                       JwtTokenProvider jwt, Clock clock) {
        this.customerRepo = customerRepo; this.staffRepo = staffRepo; this.encoder = encoder; this.jwt = jwt; this.clock = clock;
    }

    /** UC01: đăng ký; email/SĐT không trùng (BR01), mật khẩu tối thiểu 8 ký tự (BR16). */
    @Transactional
    public TokenDto register(RegisterRequest r) {
        if (r.fullName() == null || r.fullName().isBlank()) throw new BusinessException("VALIDATION_ERROR", "Vui lòng nhập họ tên");
        if ((r.email() == null || r.email().isBlank()) && (r.phone() == null || r.phone().isBlank()))
            throw new BusinessException("VALIDATION_ERROR", "Cần email hoặc số điện thoại");
        if (r.password() == null || r.password().length() < 8) throw new BusinessException("VALIDATION_ERROR", "Mật khẩu tối thiểu 8 ký tự");
        String email = blankToNull(r.email()), phone = blankToNull(r.phone());
        if (customerRepo.findByEmailOrPhone(email == null ? "-" : email, phone == null ? "-" : phone).isPresent())
            throw new BusinessException("DUPLICATE", "Email hoặc số điện thoại đã được đăng ký");   // EX-04
        Customer c = customerRepo.save(new Customer(r.fullName().trim(), email, phone, encoder.encode(r.password())));
        return new TokenDto(jwt.issue(c.getId(), "CUSTOMER"), "CUSTOMER", JwtTokenProvider.TTL.toSeconds(), c.getFullName());
    }

    private static String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }

    /** UC02: đăng nhập khách, khóa 15 phút sau 5 lần sai (BR16). */
    @Transactional(noRollbackFor = BusinessException.class)
    public TokenDto login(LoginRequest req) {
        Customer c = customerRepo.findByEmailOrPhone(req.identifier(), req.identifier())
                .orElseThrow(() -> new BusinessException("AUTH_FAILED", "Email/SĐT hoặc mật khẩu không đúng"));
        return authenticate(c, req.password(), "CUSTOMER");
    }

    /** UC13: đăng nhập nhân viên. */
    @Transactional(noRollbackFor = BusinessException.class)
    public TokenDto staffLogin(LoginRequest req) {
        Staff s = staffRepo.findByUsername(req.identifier()).filter(Staff::isActive)
                .orElseThrow(() -> new BusinessException("AUTH_FAILED", "Tên đăng nhập hoặc mật khẩu không đúng"));
        return authenticate(s, req.password(), "STAFF");
    }

    private TokenDto authenticate(User u, String rawPassword, String role) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (u.isLocked(now)) throw new BusinessException("ACCOUNT_LOCKED", "Tài khoản bị khóa đến " + u.getLockedUntil());
        if (!encoder.matches(rawPassword, u.getPasswordHash())) {
            u.recordFailedLogin(now);
            save(u);
            if (u.isLocked(now)) throw new BusinessException("ACCOUNT_LOCKED", "Sai 5 lần, tài khoản bị khóa 15 phút");
            throw new BusinessException("AUTH_FAILED", "Thông tin đăng nhập không đúng");
        }
        u.resetFailedCount();
        save(u);
        return new TokenDto(jwt.issue(u.getId(), role), role, JwtTokenProvider.TTL.toSeconds(), u.getFullName());
    }

    private void save(User u) {
        if (u instanceof Customer c) customerRepo.save(c); else staffRepo.save((Staff) u);
    }
}
