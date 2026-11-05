package vn.avq.shop.api;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.avq.shop.common.BusinessException;
import vn.avq.shop.infra.ImageStorage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/** UC15: nhân viên tải ảnh sản phẩm từ thiết bị; trả về URL dùng cho trường imageUrl khi tạo sản phẩm. */
@RestController
@RequestMapping("/api/v1/admin/uploads")
public class AdminUploadController {
    private final ImageStorage storage;
    public AdminUploadController(ImageStorage storage) { this.storage = storage; }

    @PostMapping("/images")
    public Map<String, String> upload(@RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new BusinessException("VALIDATION_ERROR", "Chưa chọn ảnh");
        try (InputStream in = file.getInputStream()) {
            return Map.of("url", storage.save(in, file.getSize()));
        }
    }
}
