package vn.avq.shop.service;

import org.springframework.stereotype.Component;
import vn.avq.shop.domain.*;
import vn.avq.shop.dto.*;
import java.util.List;

/** Ngưỡng tin cậy và quy tắc dự phòng FB-01 cho gợi ý size (AIR-08). */
@Component
public class FallbackPolicy {
    private final double minConfidence = 0.6;

    public boolean accept(AiSizeResult r) { return r != null && r.confidence() >= minConfidence; }

    /** FB-01: size đầu tiên có khoảng chiều cao, cân nặng chứa số đo; phom oversize + mặc vừa thì lùi 1 size. */
    public String ruleBasedSize(List<SizeChart> chart, BodyInput body, FitType fit) {
        int idx = -1;
        for (int i = 0; i < chart.size(); i++)
            if (chart.get(i).contains(body.heightCm(), body.weightKg())) { idx = i; break; }
        if (idx < 0) {                                   // ngoài bảng: chọn theo chiều cao gần nhất
            idx = 0;
            for (int i = 0; i < chart.size(); i++) if (body.heightCm() >= chart.get(i).getHeightMin()) idx = i;
        }
        if (fit == FitType.OVERSIZE && body.fitPref() == FitPref.VUA && idx > 0) idx--;
        return chart.get(idx).getSize();
    }
}
