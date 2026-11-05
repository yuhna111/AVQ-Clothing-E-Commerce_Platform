package vn.avq.shop.dto;

import java.time.LocalDateTime;

public record HistoryDto(String fromStatus, String toStatus, String note, String staffName, LocalDateTime changedAt) {}
