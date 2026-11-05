package vn.avq.shop.api;

import org.springframework.web.bind.annotation.*;
import vn.avq.shop.domain.OrderStatus;
import vn.avq.shop.dto.*;
import vn.avq.shop.service.OrderService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {
    private final OrderService orderService;
    public AdminOrderController(OrderService orderService) { this.orderService = orderService; }

    @GetMapping
    public List<OrderDto> list(@RequestParam(required = false) OrderStatus status) { return orderService.adminList(status); }

    @GetMapping("/{code}")
    public OrderDetailDto detail(@PathVariable String code) { return orderService.detail(code, null); }

    @PatchMapping("/{code}/status")
    public OrderDetailDto updateStatus(@RequestAttribute("uid") Long staffId, @PathVariable String code, @RequestBody UpdateStatusRequest req) {
        orderService.updateStatus(staffId, code, req.status(), req.note());
        return orderService.detail(code, null);
    }
}
