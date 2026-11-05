package vn.avq.shop.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.avq.shop.common.BusinessException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Lưu ảnh sản phẩm do nhân viên tải lên (UC15) vào thư mục cục bộ, trả về URL công khai "/uploads/{tên file}".
 * Chỉ nhận JPEG, PNG, WEBP, GIF — kiểm tra bằng chữ ký đầu file (không tin phần mở rộng hay Content-Type do trình duyệt gửi);
 * không nhận SVG/HTML để tránh chèn mã độc vào trang.
 */
@Component
public class ImageStorage {
    public static final long MAX_BYTES = 5L * 1024 * 1024;
    public static final String URL_PREFIX = "/uploads/";
    private final Path dir;

    public ImageStorage(@Value("${upload.dir:uploads}") String dir) {
        this.dir = Path.of(dir).toAbsolutePath().normalize();
        try { Files.createDirectories(this.dir); } catch (IOException e) { throw new IllegalStateException("Không tạo được thư mục ảnh " + this.dir, e); }
    }

    public Path getDir() { return dir; }

    public String save(InputStream in, long size) throws IOException {
        if (size <= 0) throw new BusinessException("VALIDATION_ERROR", "Chưa chọn ảnh");
        if (size > MAX_BYTES) throw new BusinessException("VALIDATION_ERROR", "Ảnh vượt quá 5 MB");
        byte[] data = in.readNBytes((int) MAX_BYTES + 1);
        if (data.length > MAX_BYTES) throw new BusinessException("VALIDATION_ERROR", "Ảnh vượt quá 5 MB");
        String ext = detectExtension(data);
        if (ext == null) throw new BusinessException("VALIDATION_ERROR", "Chỉ nhận ảnh JPG, PNG, WEBP hoặc GIF");
        Files.createDirectories(dir);
        String name = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Files.write(dir.resolve(name), data);
        return URL_PREFIX + name;
    }

    /** Nhận dạng định dạng ảnh theo magic bytes; null nếu không phải ảnh được phép. */
    static String detectExtension(byte[] b) {
        if (startsWith(b, 0, 0xFF, 0xD8, 0xFF)) return "jpg";
        if (startsWith(b, 0, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)) return "png";
        if (startsWith(b, 0, 'G', 'I', 'F', '8')) return "gif";
        if (startsWith(b, 0, 'R', 'I', 'F', 'F') && startsWith(b, 8, 'W', 'E', 'B', 'P')) return "webp";
        return null;
    }

    private static boolean startsWith(byte[] b, int offset, int... sig) {
        if (b.length < offset + sig.length) return false;
        for (int i = 0; i < sig.length; i++) if ((b[offset + i] & 0xFF) != sig[i]) return false;
        return true;
    }
}
