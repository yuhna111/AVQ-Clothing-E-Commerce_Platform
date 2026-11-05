package vn.avq.shop.dto;

public record TokenDto(String accessToken, String role, long expiresInSeconds, String fullName) {}
