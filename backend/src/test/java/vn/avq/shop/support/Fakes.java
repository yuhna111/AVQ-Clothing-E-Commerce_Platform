package vn.avq.shop.support;

import vn.avq.shop.domain.*;
import vn.avq.shop.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;

/** Repository giả lập trong bộ nhớ để kiểm thử tầng service không cần CSDL. */
public final class Fakes {
    private Fakes() {}

    /** Tạo repository từ đối tượng giả lập bằng proxy động: phương thức nào có trong fake thì gọi fake,
     *  nên không phụ thuộc số phương thức của JpaRepository thật (chạy được cả với stub lẫn Spring Data). */
    @SuppressWarnings("unchecked")
    public static <R> R repo(Class<R> iface, Object impl) {
        return (R) java.lang.reflect.Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[]{iface}, (proxy, m, args) -> {
            if (m.getName().equals("toString")) return "Fake" + iface.getSimpleName();
            if (m.getName().equals("hashCode")) return System.identityHashCode(impl);
            if (m.getName().equals("equals")) return proxy == args[0];
            int n = args == null ? 0 : args.length;
            for (java.lang.reflect.Method f : impl.getClass().getMethods())
                if (f.getName().equals(m.getName()) && f.getParameterCount() == n) {
                    try { return f.invoke(impl, args); } catch (java.lang.reflect.InvocationTargetException e) { throw e.getCause(); }
                }
            throw new UnsupportedOperationException(iface.getSimpleName() + "." + m.getName());
        });
    }

    public static class Mem<T> {
        final Map<Long, T> data = new LinkedHashMap<>();
        final AtomicLong seq = new AtomicLong();
        final Function<T, Long> getId; final BiConsumer<T, Long> setId;
        Mem(Function<T, Long> getId, BiConsumer<T, Long> setId) { this.getId = getId; this.setId = setId; }
        public <S extends T> S save(S e) {
            Long id = getId.apply(e);
            if (id == null) { id = seq.incrementAndGet(); setId.accept(e, id); }
            data.put(id, e); return e;
        }
        public Optional<T> findById(Long id) { return Optional.ofNullable(data.get(id)); }
        public List<T> findAll() { return new ArrayList<>(data.values()); }
        public void deleteById(Long id) { data.remove(id); }
    }
    public static class ListMem<T> {
        public final List<T> items = new ArrayList<>();
        public <S extends T> S save(S e) { if (!items.contains(e)) items.add(e); return e; }
        public Optional<T> findById(Long id) { return Optional.empty(); }
        public List<T> findAll() { return items; }
        public void deleteById(Long id) {}
    }

    public static class Customers extends Mem<Customer> {
        public Customers() { super(Customer::getId, Customer::setId); }
        public Optional<Customer> findByEmailOrPhone(String e, String p) {
            return data.values().stream().filter(c -> e.equals(c.getEmail()) || p.equals(c.getPhone())).findFirst();
        }
    }
    public static class StaffRepo extends Mem<Staff> {
        public StaffRepo() { super(Staff::getId, Staff::setId); }
        public Optional<Staff> findByUsername(String u) { return data.values().stream().filter(s -> u.equals(s.getUsername())).findFirst(); }
    }
    public static class Variants extends Mem<ProductVariant> {
        public Variants() { super(ProductVariant::getId, ProductVariant::setId); }
        public int decreaseStock(Long id, int qty) {
            ProductVariant v = data.get(id);
            synchronized (this) { if (v.getStockQty() < qty) return 0; v.setStock(v.getStockQty() - qty); return 1; }
        }
        public int increaseStock(Long id, int qty) { ProductVariant v = data.get(id); v.setStock(v.getStockQty() + qty); return 1; }
    }
    public static class Carts extends Mem<Cart> {
        public Carts() { super(Cart::getId, Cart::setId); }
        public Optional<Cart> findByCustomerId(Long cid) { return data.values().stream().filter(c -> c.getCustomer().getId().equals(cid)).findFirst(); }
    }
    public static class Orders extends Mem<Order> {
        public Orders() { super(Order::getId, Order::setId); }
        public Optional<Order> findByOrderCode(String code) { return data.values().stream().filter(o -> o.getOrderCode().equals(code)).findFirst(); }
        public Optional<Order> findByOrderCodeAndCustomerId(String code, Long cid) {
            return findByOrderCode(code).filter(o -> o.getCustomer().getId().equals(cid));
        }
        public List<Order> findByStatusAndExpiresAtBefore(OrderStatus s, LocalDateTime t) {
            return data.values().stream().filter(o -> o.getStatus() == s && o.getExpiresAt() != null && o.getExpiresAt().isBefore(t)).toList();
        }
        public List<Order> findByCustomerIdOrderByOrderDateDesc(Long cid) { return data.values().stream().filter(o -> o.getCustomer().getId().equals(cid)).toList(); }
        public List<Order> findByStatusOrderByOrderDateDesc(OrderStatus s) { return data.values().stream().filter(o -> o.getStatus() == s).toList(); }
        public List<Order> findAllByOrderByOrderDateDesc() { return new ArrayList<>(data.values()); }
    }
    public static class Payments extends Mem<Payment> {
        public Payments() { super(Payment::getId, Payment::setId); }
        public boolean existsByTxnRef(String ref) { return data.values().stream().anyMatch(p -> ref.equals(p.getTxnRef())); }
        public Optional<Payment> findFirstByOrderIdAndStatus(Long oid, PaymentStatus s) {
            return data.values().stream().filter(p -> p.getOrder().getId().equals(oid) && p.getStatus() == s).findFirst();
        }
    }
    public static class Histories extends ListMem<OrderStatusHistory> {
        public List<OrderStatusHistory> findByOrderIdOrderByChangedAt(Long oid) { return items.stream().filter(h -> h.getOrder().getId().equals(oid)).toList(); }
    }
    public static class ChangeLogs extends ListMem<ProductChangeLog> {}
    public static class Charts extends ListMem<SizeChart> {
        public List<SizeChart> findByProductIdOrderByHeightMin(Long pid) { return items.stream().filter(c -> c.getProduct().getId().equals(pid)).toList(); }
    }
    public static class Profiles {
        public final Map<Long, BodyProfile> data = new HashMap<>();
        public <S extends BodyProfile> S save(S e) { data.put(e.getCustomerId(), e); return e; }
        public Optional<BodyProfile> findById(Long id) { return Optional.ofNullable(data.get(id)); }
        public List<BodyProfile> findAll() { return new ArrayList<>(data.values()); }
        public void deleteById(Long id) { data.remove(id); }
    }
    /** Encoder giả lập thay cho BCrypt trong kiểm thử. */
    public static class PlainEncoder implements PasswordEncoder {
        public String encode(CharSequence raw) { return "{plain}" + raw; }
        public boolean matches(CharSequence raw, String enc) { return enc.equals(encode(raw)); }
    }
    /** Đồng hồ điều khiển được để kiểm thử hết hạn 15 phút và khóa tài khoản. */
    public static class MutableClock extends Clock {
        private Instant now; public MutableClock(Instant start) { now = start; }
        public void advance(Duration d) { now = now.plus(d); }
        public ZoneId getZone() { return ZoneId.of("Asia/Ho_Chi_Minh"); }
        public Clock withZone(ZoneId z) { return this; }
        public Instant instant() { return now; }
    }
}
