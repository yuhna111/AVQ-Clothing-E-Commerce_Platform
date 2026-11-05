package vn.avq.shop.service;

import org.junit.jupiter.api.*;
import vn.avq.shop.common.BusinessException;
import vn.avq.shop.dto.CartDto;
import vn.avq.shop.support.Shop;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UC07 Add to Cart (DT-02)")
class CartServiceTest {
    Shop s;
    @BeforeEach void setUp() { s = new Shop(); }

    @Test @DisplayName("Thêm dòng mới rồi cộng dồn cùng biến thể (BR07)")
    void addThenIncrease() {
        s.cartService.addToCart(s.khachA.getId(), s.aoThunM.getId(), 2);
        CartDto dto = s.cartService.addToCart(s.khachA.getId(), s.aoThunM.getId(), 3);
        assertEquals(5, dto.totalQuantity());
        assertEquals(1, s.carts.findByCustomerId(s.khachA.getId()).orElseThrow().getItems().size());
        assertEquals(BigDecimal.valueOf(1_245_000), dto.subtotal());
    }
    @Test @DisplayName("Vượt 10 sản phẩm một dòng -> QTY_LIMIT (BR05)")
    void limit10() {
        BusinessException e = assertThrows(BusinessException.class, () -> s.cartService.addToCart(s.khachA.getId(), s.aoThunM.getId(), 11));
        assertEquals("QTY_LIMIT", e.getCode());
    }
    @Test @DisplayName("Vượt tồn kho -> OUT_OF_STOCK kèm số lượng còn (EX-02)")
    void outOfStock() {
        BusinessException e = assertThrows(BusinessException.class, () -> s.cartService.addToCart(s.khachA.getId(), s.hoodieXL.getId(), 2));
        assertEquals("OUT_OF_STOCK", e.getCode());
        assertEquals("Chỉ còn 1 sản phẩm size XL màu Đen", e.getMessage());
    }
    @Test @DisplayName("Chưa chọn size, màu -> VARIANT_REQUIRED (BR04)")
    void variantRequired() {
        assertEquals("VARIANT_REQUIRED", assertThrows(BusinessException.class, () -> s.cartService.addToCart(s.khachA.getId(), null, 1)).getCode());
    }
}
