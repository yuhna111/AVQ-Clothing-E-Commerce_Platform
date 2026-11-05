package vn.avq.shop.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.avq.shop.domain.*;
import java.time.LocalDateTime;
import java.util.*;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    /** Trừ tồn kho nguyên tử: chỉ thành công khi còn đủ hàng (NFR05). Trả về số dòng bị ảnh hưởng (0 hoặc 1). */
    @Modifying
    @Query("UPDATE ProductVariant v SET v.stockQty = v.stockQty - :qty, v.version = v.version + 1 WHERE v.id = :id AND v.stockQty >= :qty")
    int decreaseStock(@Param("id") Long id, @Param("qty") int qty);

    @Modifying
    @Query("UPDATE ProductVariant v SET v.stockQty = v.stockQty + :qty, v.version = v.version + 1 WHERE v.id = :id")
    int increaseStock(@Param("id") Long id, @Param("qty") int qty);
}
