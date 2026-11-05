package vn.avq.shop.api;

import org.springframework.web.bind.annotation.*;
import vn.avq.shop.dto.*;
import vn.avq.shop.service.OrderService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping("/prepare")
    public OrderSummaryDto prepare(@RequestAttribute("uid") Long customerId) { return orderService.prepare(customerId); }

    @PostMapping
    public OrderResultDto place(@RequestAttribute("uid") Long customerId, @RequestBody PlaceOrderRequest req) {
        return orderService.place(customerId, req);
    }

    @GetMapping
    public List<OrderDto> mine(@RequestAttribute("uid") Long customerId) { return orderService.myOrders(customerId); }

    @GetMapping("/{code}")
    public OrderDetailDto detail(@RequestAttribute("uid") Long customerId, @PathVariable String code) {
        return orderService.detail(code, customerId);
    }

    @PostMapping("/{code}/cancel")
    public OrderDetailDto cancel(@RequestAttribute("uid") Long customerId, @PathVariable String code, @RequestBody CancelRequest req) {
        orderService.cancel(customerId, code, req.reason());
        return orderService.detail(code, customerId);
    }
}
