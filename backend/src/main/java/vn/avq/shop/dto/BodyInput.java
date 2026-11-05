package vn.avq.shop.dto;

import vn.avq.shop.domain.FitPref;

public record BodyInput(int heightCm, int weightKg, FitPref fitPref, boolean saveProfile) {}
