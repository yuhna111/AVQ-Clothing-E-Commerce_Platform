import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Khi chạy "npm run dev", mọi lời gọi /api và ảnh đã tải lên /uploads được chuyển tới backend Spring Boot ở cổng 8080.
export default defineConfig({
  plugins: [react()],
  // strictPort: nếu 5173 đang bận thì báo lỗi, không tự chạy thêm một bản ở 5174 (bản cũ có thể thiếu cấu hình proxy mới).
  server: { port: 5173, strictPort: true, proxy: { "/api": "http://localhost:8080", "/uploads": "http://localhost:8080" } },
});
