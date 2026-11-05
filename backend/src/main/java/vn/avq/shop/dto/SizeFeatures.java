package vn.avq.shop.dto;

import java.util.List;

public record SizeFeatures(int heightCm, int weightKg, String fitPref, String fitType,
                           List<String> sizes, List<int[]> heightRanges, List<String> keptSizes) {}
