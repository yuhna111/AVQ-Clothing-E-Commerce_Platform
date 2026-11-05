package vn.avq.shop.infra;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.avq.shop.common.BusinessException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ImageStorageTest {
    @TempDir Path tmp;

    private String save(ImageStorage s, byte[] data) throws Exception {
        return s.save(new ByteArrayInputStream(data), data.length);
    }

    @Test
    void luuAnhPngVaTraVeUrlUploads() throws Exception {
        ImageStorage s = new ImageStorage(tmp.toString());
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
        String url = save(s, png);
        assertTrue(url.matches("/uploads/[0-9a-f]{32}\\.png"), url);
        assertArrayEquals(png, Files.readAllBytes(tmp.resolve(url.substring("/uploads/".length()))));
    }

    @Test
    void nhanDangJpegWebpGif() {
        assertEquals("jpg", ImageStorage.detectExtension(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0}));
        assertEquals("gif", ImageStorage.detectExtension("GIF89a".getBytes(StandardCharsets.US_ASCII)));
        assertEquals("webp", ImageStorage.detectExtension("RIFF\0\0\0\0WEBPVP8 ".getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    void tuChoiSvgVaHtmlDuDatTenAnh() {
        ImageStorage s = new ImageStorage(tmp.toString());
        BusinessException e = assertThrows(BusinessException.class,
                () -> save(s, "<svg onload=alert(1)></svg>".getBytes(StandardCharsets.UTF_8)));
        assertEquals("VALIDATION_ERROR", e.getCode());
    }

    @Test
    void tuChoiAnhQua5MB() {
        ImageStorage s = new ImageStorage(tmp.toString());
        byte[] big = new byte[(int) ImageStorage.MAX_BYTES + 1];
        big[0] = (byte) 0xFF; big[1] = (byte) 0xD8; big[2] = (byte) 0xFF;
        assertThrows(BusinessException.class, () -> save(s, big));
    }
}
