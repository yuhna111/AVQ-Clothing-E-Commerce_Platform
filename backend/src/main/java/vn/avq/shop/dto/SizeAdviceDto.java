package vn.avq.shop.dto;

public record SizeAdviceDto(String size, Double confidence, String reason, String source) {
    public static SizeAdviceDto ai(String size, double c, String reason) { return new SizeAdviceDto(size, c, reason, "AI"); }
    public static SizeAdviceDto byChart(String size) { return new SizeAdviceDto(size, null, "Theo bảng size", "CHART"); }
    public static SizeAdviceDto contactShop() { return new SizeAdviceDto(null, null, "Liên hệ cửa hàng để được tư vấn size", "NONE"); }
}
