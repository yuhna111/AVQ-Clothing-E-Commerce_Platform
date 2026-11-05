package vn.avq.shop.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.avq.shop.common.BusinessException;
import vn.avq.shop.domain.*;
import vn.avq.shop.dto.Actor;
import vn.avq.shop.repository.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import static vn.avq.shop.domain.OrderStatus.*;

/** DT-04 / BR14: nơi duy nhất kiểm tra và thực hiện chuyển trạng thái đơn. */
@Component
public class OrderStateMachine {
    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            CHO_THANH_TOAN, EnumSet.of(CHO_XAC_NHAN, DA_HUY),
            CHO_XAC_NHAN,   EnumSet.of(DANG_GIAO, DA_HUY),
            DANG_GIAO,      EnumSet.of(DA_GIAO),
            DA_GIAO,        EnumSet.noneOf(OrderStatus.class),
            DA_HUY,         EnumSet.noneOf(OrderStatus.class));

    private final OrderStatusHistoryRepository historyRepo;
    private final StaffRepository staffRepo;
    private final StockService stockService;
    private final Clock clock;

    public OrderStateMachine(OrderStatusHistoryRepository historyRepo, StaffRepository staffRepo,
                             StockService stockService, Clock clock) {
        this.historyRepo = historyRepo; this.staffRepo = staffRepo; this.stockService = stockService; this.clock = clock;
    }

    public boolean canTransit(OrderStatus from, OrderStatus to) { return TRANSITIONS.get(from).contains(to); }

    public Set<OrderStatus> nextStates(OrderStatus from) { return TRANSITIONS.get(from); }

    @Transactional
    public void transit(Order order, OrderStatus to, Actor actor, String note) {
        OrderStatus from = order.getStatus();
        if (!canTransit(from, to)) throw new BusinessException("INVALID_TRANSITION", from + " -> " + to);
        if (to == DA_HUY && actor.isStaff() && (note == null || note.isBlank()))
            throw new BusinessException("CANCEL_REASON_REQUIRED", "Nhân viên hủy đơn phải ghi lý do");   // UC19 2a
        if (to == DA_HUY) stockService.restore(order.getItems());                                       // BR11
        order.setStatus(to);
        order.setExpiresAt(null);
        Staff staff = actor.isStaff() ? staffRepo.findById(actor.staffId()).orElse(null) : null;
        historyRepo.save(OrderStatusHistory.of(order, from, to, staff, note, LocalDateTime.now(clock)));
    }

    public List<vn.avq.shop.dto.HistoryDto> history(Long orderId) {
        return historyRepo.findByOrderIdOrderByChangedAt(orderId).stream()
                .map(h -> new vn.avq.shop.dto.HistoryDto(h.getFromStatus(), h.getToStatus(), h.getNote(), h.getStaffName(), h.getChangedAt()))
                .toList();
    }

    /** Ghi bản ghi lịch sử đầu tiên khi đơn vừa được tạo. */
    public void recordCreated(Order order) {
        historyRepo.save(OrderStatusHistory.of(order, null, order.getStatus(), null, "Tạo đơn", LocalDateTime.now(clock)));
    }
}
