package vn.avq.shop.api;

import org.springframework.web.bind.annotation.*;
import vn.avq.shop.dto.*;
import vn.avq.shop.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public TokenDto register(@RequestBody RegisterRequest req) { return authService.register(req); }

    @PostMapping("/login")
    public TokenDto login(@RequestBody LoginRequest req) { return authService.login(req); }

    @PostMapping("/staff/login")
    public TokenDto staffLogin(@RequestBody LoginRequest req) { return authService.staffLogin(req); }
}
