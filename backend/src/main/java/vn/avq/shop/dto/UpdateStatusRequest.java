package vn.avq.shop.dto;

import vn.avq.shop.domain.OrderStatus;

public record UpdateStatusRequest(OrderStatus status, String note) {}
