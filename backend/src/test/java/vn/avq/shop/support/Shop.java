package vn.avq.shop.support;

import vn.avq.shop.domain.*;
import vn.avq.shop.infra.*;
import vn.avq.shop.repository.*;
import vn.avq.shop.service.*;
import java.math.BigDecimal;
import java.time.*;

/** Lắp ráp các service thật với repository giả lập và dữ liệu mẫu giống avq_seed.sql. */
public class Shop {
    public final Fakes.MutableClock clock = new Fakes.MutableClock(Instant.parse("2026-10-28T02:00:00Z"));
    public final Fakes.Customers customers = new Fakes.Customers();
    public final Fakes.StaffRepo staff = new Fakes.StaffRepo();
    public final Fakes.Variants variants = new Fakes.Variants();
    public final Fakes.Carts carts = new Fakes.Carts();
    public final Fakes.Orders orders = new Fakes.Orders();
    public final Fakes.Payments payments = new Fakes.Payments();
    public final Fakes.Histories histories = new Fakes.Histories();
    public final Fakes.ChangeLogs logs = new Fakes.ChangeLogs();
    public final CustomerRepository customerRepo = Fakes.repo(CustomerRepository.class, customers);
    public final StaffRepository staffRepo = Fakes.repo(StaffRepository.class, staff);
    public final ProductVariantRepository variantRepo = Fakes.repo(ProductVariantRepository.class, variants);
    public final CartRepository cartRepo = Fakes.repo(CartRepository.class, carts);
    public final OrderRepository orderRepo = Fakes.repo(OrderRepository.class, orders);
    public final PaymentRepository paymentRepo = Fakes.repo(PaymentRepository.class, payments);
    public final OrderStatusHistoryRepository historyRepo = Fakes.repo(OrderStatusHistoryRepository.class, histories);
    public final ProductChangeLogRepository logRepo = Fakes.repo(ProductChangeLogRepository.class, logs);
    public final VnPayClient vnpay = new VnPayClient("AVQTEST1", "SECRETKEY-SANDBOX", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html", "https://avqclothing.vn/thanh-toan/ket-qua");
    public final StockService stock = new StockService(variantRepo, logRepo, staffRepo, clock);
    public final OrderStateMachine stateMachine = new OrderStateMachine(historyRepo, staffRepo, stock, clock);
    public final PaymentService paymentService = new PaymentService(vnpay, paymentRepo, orderRepo, stateMachine, clock);
    public final CartService cartService = new CartService(cartRepo, customerRepo, variantRepo);
    public final OrderService orderService = new OrderService(orderRepo, cartRepo, customerRepo, new ShippingFeeCalculator(), stock, stateMachine, paymentService, clock);

    public final Customer khachA; public final Address diaChi; public final Staff nhanVien;
    public final ProductVariant hoodieL, hoodieXL, aoThunM;

    public Shop() {
        khachA = customers.save(new Customer("Nguyễn Văn A", "a@avq.vn", "0901234567", "{plain}matkhau123"));
        diaChi = new Address("Nguyễn Văn A", "0901234567", "12 Nguyễn Trãi, Q.1, TP.HCM"); diaChi.setId(12L); khachA.addAddress(diaChi);
        nhanVien = staff.save(new Staff("Trần Nhân Viên", "nhanvien1", "{plain}staff@123"));
        Category hoodie = new Category("Hoodie", "hoodie"), ao = new Category("Áo thun", "ao-thun");
        Product hd = new Product(hoodie, "Hoodie Basic Nỉ Bông", BigDecimal.valueOf(459_000), FitType.OVERSIZE);
        Product at = new Product(ao, "Áo thun Basic Oversize", BigDecimal.valueOf(249_000), FitType.OVERSIZE);
        hd.setId(2L); at.setId(1L);
        hoodieL = variants.save(hd.addVariant("L", "Đen", "HD-BSC-L-DE", 7));
        hoodieXL = variants.save(hd.addVariant("XL", "Đen", "HD-BSC-XL-DE", 1));
        aoThunM = variants.save(at.addVariant("M", "Trắng", "AT-BSC-M-TR", 12));
    }
    public LocalDateTime now() { return LocalDateTime.now(clock); }
}
