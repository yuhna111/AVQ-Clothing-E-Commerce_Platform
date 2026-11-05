package vn.avq.shop.infra;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.avq.shop.service.OrderService;
import java.util.logging.Logger;

@Component
public class OrderExpiryScheduler {
    private static final Logger log = Logger.getLogger(OrderExpiryScheduler.class.getName());
    private final OrderService orderService;

    public OrderExpiryScheduler(OrderService orderService) { this.orderService = orderService; }

    @Scheduled(fixedDelay = 60_000)                                                // BR12
    public void run() {
        int n = orderService.expirePendingOrders();
        if (n > 0) log.info("Đã hủy " + n + " đơn quá hạn thanh toán");
    }
}
