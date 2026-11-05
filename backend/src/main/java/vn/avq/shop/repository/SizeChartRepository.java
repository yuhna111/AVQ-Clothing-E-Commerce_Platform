package vn.avq.shop.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.avq.shop.domain.*;
import java.time.LocalDateTime;
import java.util.*;

public interface SizeChartRepository extends JpaRepository<SizeChart, Long> {
    List<SizeChart> findByProductIdOrderByHeightMin(Long productId);
}
