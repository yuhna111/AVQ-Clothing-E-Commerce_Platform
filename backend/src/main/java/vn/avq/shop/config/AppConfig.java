package vn.avq.shop.config;

import org.springframework.context.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Clock;

@Configuration
public class AppConfig {
    @Bean public Clock clock() { return Clock.systemDefaultZone(); }
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(10); }   // NFR03
}
