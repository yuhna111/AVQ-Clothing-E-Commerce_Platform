package vn.avq.shop.common;

/** Lỗi nghiệp vụ có mã (OUT_OF_STOCK, PRICE_CHANGED, INVALID_TRANSITION...) — ánh xạ HTTP ở GlobalExceptionHandler. */
public class BusinessException extends RuntimeException {
    private final String code;
    public BusinessException(String code) { this(code, code); }
    public BusinessException(String code, String message) { super(message); this.code = code; }
    public String getCode() { return code; }
}
