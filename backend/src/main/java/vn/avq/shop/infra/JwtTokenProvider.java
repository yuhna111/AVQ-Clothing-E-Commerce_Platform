package vn.avq.shop.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.avq.shop.dto.AuthPrincipal;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.Base64;

/** JWT HS256 tối giản: sub = userId, role, exp. */
@Component
public class JwtTokenProvider {
    public static final Duration TTL = Duration.ofMinutes(30);                    // NFR04
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private final byte[] secret;
    private final Clock clock;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret, Clock clock) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8); this.clock = clock;
    }

    public String issue(Long userId, String role) {
        long exp = Instant.now(clock).plus(TTL).getEpochSecond();
        String header = B64.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String payload = B64.encodeToString(String.format("{\"sub\":\"%d\",\"role\":\"%s\",\"exp\":%d}", userId, role, exp)
                .getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + "." + sign(header + "." + payload);
    }

    public AuthPrincipal parse(String token) {
        String[] p = token.split("\\.");
        if (p.length != 3 || !MessageDigest.isEqual(sign(p[0] + "." + p[1]).getBytes(), p[2].getBytes()))
            throw new IllegalArgumentException("Token không hợp lệ");
        String json = new String(Base64.getUrlDecoder().decode(p[1]), StandardCharsets.UTF_8);
        long exp = Long.parseLong(json.replaceAll(".*\"exp\":(\\d+).*", "$1"));
        if (Instant.now(clock).getEpochSecond() > exp) throw new IllegalArgumentException("Token hết hạn");
        return new AuthPrincipal(Long.parseLong(json.replaceAll(".*\"sub\":\"(\\d+)\".*", "$1")),
                json.replaceAll(".*\"role\":\"(\\w+)\".*", "$1"));
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return B64.encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
