package vn.avq.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.avq.shop.common.BusinessException;
import vn.avq.shop.domain.*;
import vn.avq.shop.repository.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockService {
    private final ProductVariantRepository variantRepo;
    private final ProductChangeLogRepository logRepo;
    private final StaffRepository staffRepo;
    private final Clock clock;

    public StockService(ProductVariantRepository variantRepo, ProductChangeLogRepository logRepo,
                        StaffRepository staffRepo, Clock clock) {
        this.variantRepo = variantRepo; this.logRepo = logRepo; this.staffRepo = staffRepo; this.clock = clock;
    }

    /** Trừ tồn kho từng dòng; thiếu hàng thì ném lỗi để @Transactional hoàn tác cả đơn (BR11, NFR05). */
    @Transactional
    public void reserve(List<OrderItem> items) {
        for (OrderItem line : items) {
            if (variantRepo.decreaseStock(line.getVariant().getId(), line.getQuantity()) == 0)
                throw new BusinessException("OUT_OF_STOCK", "Hết hàng: " + line.getVariant().getSku());
        }
    }

    /** Hoàn tồn kho khi đơn bị hủy (BR11). */
    @Transactional
    public void restore(List<OrderItem> items) {
        for (OrderItem line : items) variantRepo.increaseStock(line.getVariant().getId(), line.getQuantity());
    }

    /** UC17: nhân viên cập nhật tồn kho, ghi nhật ký giá trị cũ/mới (FR30, FR31). */
    @Transactional
    public void updateStock(Long staffId, Long variantId, int newQty) {
        if (newQty < 0) throw new BusinessException("VALIDATION_ERROR", "Tồn kho phải là số nguyên không âm");
        ProductVariant v = variantRepo.findById(variantId).orElseThrow(() -> new BusinessException("NOT_FOUND"));
        Staff staff = staffRepo.findById(staffId).orElseThrow(() -> new BusinessException("FORBIDDEN"));
        int old = v.getStockQty();
        v.setStock(newQty);
        variantRepo.save(v);
        logRepo.save(ProductChangeLog.of(staff, v, "stock_qty", String.valueOf(old), String.valueOf(newQty), LocalDateTime.now(clock)));
    }
}
