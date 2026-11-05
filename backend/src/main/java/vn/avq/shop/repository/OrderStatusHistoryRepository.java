package vn.avq.shop.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.avq.shop.domain.*;
import java.time.LocalDateTime;
import java.util.*;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    List<OrderStatusHistory> findByOrderIdOrderByChangedAt(Long orderId);
}
