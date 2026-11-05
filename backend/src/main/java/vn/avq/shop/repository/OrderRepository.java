package vn.avq.shop.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.avq.shop.domain.*;
import java.time.LocalDateTime;
import java.util.*;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);
    Optional<Order> findByOrderCodeAndCustomerId(String orderCode, Long customerId);
    List<Order> findByStatusAndExpiresAtBefore(OrderStatus status, LocalDateTime time);
    List<Order> findByCustomerIdOrderByOrderDateDesc(Long customerId);
    List<Order> findByStatusOrderByOrderDateDesc(OrderStatus status);
    List<Order> findAllByOrderByOrderDateDesc();
}
