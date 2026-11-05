package vn.avq.shop.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.avq.shop.common.BusinessException;
import vn.avq.shop.dto.ErrorResponse;
import java.util.Map;

/** Định dạng lỗi thống nhất (Chương 4): mã nghiệp vụ -> mã HTTP. */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Map<String, Integer> STATUS = Map.ofEntries(
            Map.entry("VALIDATION_ERROR", 400), Map.entry("VARIANT_REQUIRED", 400), Map.entry("CANCEL_REASON_REQUIRED", 400),
            Map.entry("AUTH_FAILED", 401), Map.entry("ACCOUNT_LOCKED", 401), Map.entry("FORBIDDEN", 403),
            Map.entry("NOT_FOUND", 404), Map.entry("CART_EMPTY", 409), Map.entry("OUT_OF_STOCK", 409),
            Map.entry("QTY_LIMIT", 409), Map.entry("PRICE_CHANGED", 409), Map.entry("INVALID_TRANSITION", 409),
            Map.entry("DUPLICATE", 409));

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handle(BusinessException e) {
        return ResponseEntity.status(STATUS.getOrDefault(e.getCode(), 400)).body(new ErrorResponse(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handle(org.springframework.web.multipart.MaxUploadSizeExceededException e) {
        return ResponseEntity.status(400).body(new ErrorResponse("VALIDATION_ERROR", "Ảnh vượt quá 5 MB"));
    }

    /** Lưới an toàn: vi phạm ràng buộc UNIQUE/khóa ngoại mà tầng service chưa bắt được. */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handle(org.springframework.dao.DataIntegrityViolationException e) {
        return ResponseEntity.status(409).body(new ErrorResponse("DUPLICATE", "Dữ liệu bị trùng hoặc không hợp lệ (ví dụ mã SKU đã tồn tại)"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handle(IllegalArgumentException e) {
        return ResponseEntity.status(400).body(new ErrorResponse("VALIDATION_ERROR", e.getMessage()));
    }
}
