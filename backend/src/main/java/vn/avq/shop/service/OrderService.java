package vn.avq.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.avq.shop.common.BusinessException;
import vn.avq.shop.domain.*;
import vn.avq.shop.dto.*;
import vn.avq.shop.repository.*;
import java.time.*;
import java.util.List;
import vn.avq.shop.dto.OrderLineDto;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {
    static final Duration HOLD = Duration.ofMinutes(15);                                  // BR12
    private static final AtomicLong SEQ = new AtomicLong();

    private final OrderRepository orderRepo;
    private final CartRepository cartRepo;
    private final CustomerRepository customerRepo;
    private final ShippingFeeCalculator feeCalc;
    private final StockService stockService;
    private final OrderStateMachine stateMachine;
    private final PaymentService paymentService;
    private final Clock clock;

    public OrderService(OrderRepository orderRepo, CartRepository cartRepo, CustomerRepository customerRepo,
                        ShippingFeeCalculator feeCalc, StockService stockService, OrderStateMachine stateMachine,
                        PaymentService paymentService, Clock clock) {
        this.orderRepo = orderRepo; this.cartRepo = cartRepo; this.customerRepo = customerRepo; this.feeCalc = feeCalc;
        this.stockService = stockService; this.stateMachine = stateMachine; this.paymentService = paymentService; this.clock = clock;
    }

    /** UC09 bước 2–4: kiểm tra lại giá, tồn kho từng dòng và tính tổng (FR17, FR18). */
    @Transactional
    public OrderSummaryDto prepare(Long customerId) {
        Cart cart = loadCart(customerId);
        for (CartItem i : cart.getItems())
            if (!i.getVariant().isAvailable(i.getQuantity()))
                throw new BusinessException("OUT_OF_STOCK", "Hết hàng: " + i.getVariant().getSku());       // EX-06
        var subtotal = cart.subtotal();
        var fee = feeCalc.calculate(subtotal);
        return new OrderSummaryDto(subtotal, fee, subtotal.add(fee));
    }

    /** UC09: tạo đơn, chốt giá, trừ tồn kho, dọn giỏ trong một giao dịch (FR22, NFR05). */
    @Transactional
    public OrderResultDto place(Long customerId, PlaceOrderRequest req) {
        Cart cart = loadCart(customerId);
        Customer customer = customerRepo.findById(customerId).orElseThrow();
        Address address = customer.getAddresses().stream().filter(a -> a.getId().equals(req.addressId()))
                .findFirst().orElseThrow(() -> new BusinessException("NOT_FOUND", "Địa chỉ không hợp lệ"));
        LocalDateTime now = LocalDateTime.now(clock);

        String code = "AVQ-" + now.format(java.time.format.DateTimeFormatter.ofPattern("yyMMddHHmmss")) + String.format("%02d", SEQ.incrementAndGet() % 100);
        Order order = Order.newOrder(code, customer, address, req.paymentMethod(), now);
        for (CartItem item : cart.getItems()) {
            if (!item.getVariant().getProduct().isVisible())
                throw new BusinessException("OUT_OF_STOCK", "Ngừng bán: " + item.getVariant().getSku());
            order.addItem(item.getVariant(), item.getQuantity());                       // chốt giá hiện hành (BR08)
        }
        order.recalculate();
        order.setShippingFee(feeCalc.calculate(order.getSubtotal()));
        order.recalculate();
        if (req.expectedTotal() != null && order.getTotal().compareTo(req.expectedTotal()) != 0)
            throw new BusinessException("PRICE_CHANGED", "Tổng tiền mới: " + order.getTotal().toPlainString());

        stockService.reserve(order.getItems());                                         // BR11: giữ hàng ngay
        if (req.paymentMethod() == PaymentMethod.COD) {
            order.setStatus(OrderStatus.CHO_XAC_NHAN);                                  // BR10
        } else {
            order.setStatus(OrderStatus.CHO_THANH_TOAN);
            order.setExpiresAt(now.plus(HOLD));
        }
        orderRepo.save(order);
        stateMachine.recordCreated(order);
        cart.removeItems(order.getVariantIds());                                        // FR22
        cartRepo.save(cart);
        String url = order.getPaymentMethod() == PaymentMethod.ONLINE ? paymentService.createPayment(order) : null;
        return OrderResultDto.from(order, url);
    }

    /** UC12: khách hủy đơn ở trạng thái cho phép (BR13). */
    @Transactional
    public void cancel(Long customerId, String orderCode, String reason) {
        Order o = orderRepo.findByOrderCodeAndCustomerId(orderCode, customerId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND"));
        stateMachine.transit(o, OrderStatus.DA_HUY, Actor.customer(), reason);
        orderRepo.save(o);
    }

    /** UC19: nhân viên chuyển trạng thái; lưu mã vận đơn khi sang Đang giao. */
    @Transactional
    public void updateStatus(Long staffId, String orderCode, OrderStatus to, String note) {
        Order o = orderRepo.findByOrderCode(orderCode).orElseThrow(() -> new BusinessException("NOT_FOUND"));
        if (o.getStatus() == OrderStatus.CHO_THANH_TOAN)                                 // DT-03
            throw new BusinessException("INVALID_TRANSITION", "Đơn chờ thanh toán do hệ thống xử lý");
        if (to == OrderStatus.DANG_GIAO) o.setTrackingCode(note);
        stateMachine.transit(o, to, Actor.staff(staffId), note);
        orderRepo.save(o);
    }

    /** BR12: hủy các đơn Chờ thanh toán đã quá hạn; gọi từ OrderExpiryScheduler. */
    @Transactional
    public int expirePendingOrders() {
        List<Order> expired = orderRepo.findByStatusAndExpiresAtBefore(OrderStatus.CHO_THANH_TOAN, LocalDateTime.now(clock));
        for (Order o : expired) {
            stateMachine.transit(o, OrderStatus.DA_HUY, Actor.SYSTEM, "Hết hạn thanh toán 15 phút");
            orderRepo.save(o);
        }
        return expired.size();
    }

    /** UC11: đơn của tôi. */
    @Transactional(readOnly = true)
    public List<OrderDto> myOrders(Long customerId) {
        return orderRepo.findByCustomerIdOrderByOrderDateDesc(customerId).stream().map(OrderService::summary).toList();
    }

    /** UC18: danh sách đơn cho nhân viên, lọc theo trạng thái. */
    @Transactional(readOnly = true)
    public List<OrderDto> adminList(OrderStatus status) {
        return (status == null ? orderRepo.findAllByOrderByOrderDateDesc() : orderRepo.findByStatusOrderByOrderDateDesc(status))
                .stream().map(OrderService::summary).toList();
    }

    @Transactional(readOnly = true)
    public OrderDetailDto detail(String orderCode, Long customerIdOrNull) {
        Order o = (customerIdOrNull == null ? orderRepo.findByOrderCode(orderCode)
                : orderRepo.findByOrderCodeAndCustomerId(orderCode, customerIdOrNull))
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Không tìm thấy đơn"));
        return new OrderDetailDto(o.getOrderCode(), o.getStatus().name(), o.getPaymentMethod().name(), o.getOrderDate(), o.getExpiresAt(),
                o.getReceiverName(), o.getReceiverPhone(), o.getShippingAddress(), o.getTrackingCode(),
                o.getSubtotal(), o.getShippingFee(), o.getTotal(),
                o.getItems().stream().map(i -> new OrderLineDto(i.getProductName(), i.getSize(), i.getColor(), i.getUnitPrice(),
                        i.getQuantity(), i.lineTotal())).toList(),
                stateMachine.history(o.getId()),
                stateMachine.nextStates(o.getStatus()).stream().map(Enum::name).toList(),
                o.getStatus() == OrderStatus.CHO_THANH_TOAN || o.getStatus() == OrderStatus.CHO_XAC_NHAN);
    }

    static OrderDto summary(Order o) {
        return new OrderDto(o.getOrderCode(), o.getOrderDate(), o.getStatus().name(), o.getPaymentMethod().name(), o.getTotal(),
                o.getItems().stream().mapToInt(OrderItem::getQuantity).sum(), o.getReceiverName());
    }

    private Cart loadCart(Long customerId) {
        Cart cart = cartRepo.findByCustomerId(customerId).orElseThrow(() -> new BusinessException("CART_EMPTY"));
        if (cart.getItems().isEmpty()) throw new BusinessException("CART_EMPTY");
        return cart;
    }
}
