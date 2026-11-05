package vn.avq.shop.service;

import org.junit.jupiter.api.*;
import vn.avq.shop.common.BusinessException;
import vn.avq.shop.dto.*;
import vn.avq.shop.infra.JwtTokenProvider;
import vn.avq.shop.support.*;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UC02 Login (BR16, NFR04)")
class AuthServiceTest {
    Shop s; AuthService auth; JwtTokenProvider jwt;
    @BeforeEach void setUp() {
        s = new Shop(); jwt = new JwtTokenProvider("avq-jwt-secret-key-32-bytes-long!!", s.clock);
        auth = new AuthService(s.customerRepo, s.staffRepo, new Fakes.PlainEncoder(), jwt, s.clock);
    }
    @Test @DisplayName("Đăng nhập đúng -> JWT chứa userId và role CUSTOMER")
    void ok() {
        TokenDto t = auth.login(new LoginRequest("a@avq.vn", "matkhau123"));
        assertEquals(s.khachA.getId(), jwt.parse(t.accessToken()).userId());
        assertEquals("CUSTOMER", jwt.parse(t.accessToken()).role());
    }
    @Test @DisplayName("Sai 5 lần -> khóa 15 phút; mật khẩu đúng cũng bị từ chối khi đang khóa")
    void lockAfter5() {
        for (int i = 1; i <= 4; i++)
            assertEquals("AUTH_FAILED", assertThrows(BusinessException.class, () -> auth.login(new LoginRequest("0901234567", "sai"))).getCode());
        assertEquals("ACCOUNT_LOCKED", assertThrows(BusinessException.class, () -> auth.login(new LoginRequest("0901234567", "sai"))).getCode());
        assertEquals("ACCOUNT_LOCKED", assertThrows(BusinessException.class, () -> auth.login(new LoginRequest("a@avq.vn", "matkhau123"))).getCode());
        s.clock.advance(Duration.ofMinutes(16));
        assertNotNull(auth.login(new LoginRequest("a@avq.vn", "matkhau123")).accessToken());
    }
    @Test @DisplayName("Token hết hạn sau 30 phút")
    void tokenExpires() {
        String t = auth.login(new LoginRequest("a@avq.vn", "matkhau123")).accessToken();
        s.clock.advance(Duration.ofMinutes(31));
        assertThrows(IllegalArgumentException.class, () -> jwt.parse(t));
    }
}
