package vn.avq.shop.dto;

public record Actor(Long staffId, String type) {
    public static final Actor SYSTEM = new Actor(null, "SYSTEM");
    public static Actor customer() { return new Actor(null, "CUSTOMER"); }
    public static Actor staff(Long id) { return new Actor(id, "STAFF"); }
    public boolean isStaff() { return "STAFF".equals(type); }
}
