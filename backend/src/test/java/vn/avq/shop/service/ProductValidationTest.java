package vn.avq.shop.service;

import org.junit.jupiter.api.Test;
import vn.avq.shop.common.BusinessException;
import vn.avq.shop.dto.CreateProductRequest;
import vn.avq.shop.dto.CreateProductRequest.NewVariant;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** UC15: dữ liệu sản phẩm mới phải được kiểm tra ở tầng service, không để MySQL từ chối với lỗi khó hiểu. */
class ProductValidationTest {
    private static CreateProductRequest req(NewVariant... vs) {
        return new CreateProductRequest(1L, "Áo thử", "", "", "REGULAR", BigDecimal.valueOf(150000), "", List.of(vs));
    }

    private static BusinessException fail(CreateProductRequest r) {
        return assertThrows(BusinessException.class, () -> ProductService.validateNew(r));
    }

    @Test
    void hopLe() {
        assertDoesNotThrow(() -> ProductService.validateNew(req(new NewVariant("M", "Đen", "A-M-DEN", 5), new NewVariant("L", "Đen", "A-L-DEN", 5))));
    }

    @Test
    void haiBienTheDeTrongSkuBaoLoiRoRang() {
        BusinessException e = fail(req(new NewVariant("M", "Đen", "", 5), new NewVariant("L", "Đen", "", 5)));
        assertEquals("VALIDATION_ERROR", e.getCode());
        assertTrue(e.getMessage().contains("SKU"), e.getMessage());
    }

    @Test
    void trungSkuTrongCungForm() {
        BusinessException e = fail(req(new NewVariant("M", "Đen", "A-1", 5), new NewVariant("L", "Đen", "a-1", 5)));
        assertEquals("DUPLICATE", e.getCode());
    }

    @Test
    void trungSizeMau() {
        assertEquals("DUPLICATE", fail(req(new NewVariant("M", "Đen", "A-1", 5), new NewVariant("M", "đen", "A-2", 5))).getCode());
    }

    @Test
    void thieuDanhMucTenGiaHoacBienThe() {
        NewVariant v = new NewVariant("M", "Đen", "A-1", 5);
        assertEquals("VALIDATION_ERROR", fail(new CreateProductRequest(null, "X", "", "", null, BigDecimal.TEN, "", List.of(v))).getCode());
        assertEquals("VALIDATION_ERROR", fail(new CreateProductRequest(1L, " ", "", "", null, BigDecimal.TEN, "", List.of(v))).getCode());
        assertEquals("VALIDATION_ERROR", fail(new CreateProductRequest(1L, "X", "", "", null, BigDecimal.ZERO, "", List.of(v))).getCode());
        assertEquals("VALIDATION_ERROR", fail(new CreateProductRequest(1L, "X", "", "", null, BigDecimal.TEN, "", List.of())).getCode());
    }
}
