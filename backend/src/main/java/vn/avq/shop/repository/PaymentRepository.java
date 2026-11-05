package vn.avq.shop.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.avq.shop.domain.*;
import java.time.LocalDateTime;
import java.util.*;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByTxnRef(String txnRef);
    Optional<Payment> findFirstByOrderIdAndStatus(Long orderId, PaymentStatus status);
}
