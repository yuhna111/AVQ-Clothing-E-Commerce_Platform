package vn.avq.shop.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.avq.shop.domain.*;
import java.time.LocalDateTime;
import java.util.*;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("""
        SELECT DISTINCT p FROM Product p JOIN p.variants v
        WHERE p.visible = true
          AND (:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
          AND (:size IS NULL OR v.size = :size)
          AND (:color IS NULL OR v.color = :color)
          AND (:minPrice IS NULL OR p.price >= :minPrice)
          AND (:maxPrice IS NULL OR p.price <= :maxPrice)
          AND v.stockQty > 0
        """)
    List<Product> search(@Param("q") String q, @Param("categoryId") Long categoryId, @Param("size") String size,
                         @Param("color") String color, @Param("minPrice") java.math.BigDecimal minPrice,
                         @Param("maxPrice") java.math.BigDecimal maxPrice);

    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v WHERE p.id IN :ids AND p.visible = true AND v.stockQty > 0")
    List<Product> findVisibleInStock(@Param("ids") List<Long> ids);
}
