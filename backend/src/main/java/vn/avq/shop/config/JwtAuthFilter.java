package vn.avq.shop.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.avq.shop.dto.AuthPrincipal;
import vn.avq.shop.infra.JwtTokenProvider;
import java.io.IOException;

/**
 * Đọc JWT ở header Authorization, đặt request attribute "uid", "role" cho controller.
 * Phân quyền (BR15, NFR04): /admin/** chỉ STAFF; giỏ, đơn, địa chỉ chỉ CUSTOMER; còn lại công khai.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwt;
    public JwtAuthFilter(JwtTokenProvider jwt) { this.jwt = jwt; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String path = req.getRequestURI();
        String header = req.getHeader("Authorization");
        AuthPrincipal p = null;
        if (header != null && header.startsWith("Bearer ")) {
            try { p = jwt.parse(header.substring(7)); } catch (IllegalArgumentException e) { p = null; }
        }
        if (p != null) { req.setAttribute("uid", p.userId()); req.setAttribute("role", p.role()); }
        boolean adminApi = path.startsWith("/api/v1/admin/");
        boolean customerApi = path.startsWith("/api/v1/cart") || path.startsWith("/api/v1/orders") || path.startsWith("/api/v1/addresses");
        if (adminApi && (p == null || !"STAFF".equals(p.role()))) { deny(res, p == null ? 401 : 403); return; }
        if (customerApi && (p == null || !"CUSTOMER".equals(p.role()))) { deny(res, p == null ? 401 : 403); return; }
        chain.doFilter(req, res);
    }

    private static void deny(HttpServletResponse res, int status) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(status == 401 ? "{\"code\":\"AUTH_REQUIRED\",\"message\":\"Vui lòng đăng nhập\"}"
                                           : "{\"code\":\"FORBIDDEN\",\"message\":\"Không có quyền truy cập\"}");
    }
}
