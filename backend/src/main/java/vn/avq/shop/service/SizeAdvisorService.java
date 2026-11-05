package vn.avq.shop.service;

import org.springframework.stereotype.Service;
import vn.avq.shop.domain.*;
import vn.avq.shop.dto.*;
import vn.avq.shop.infra.AiServiceClient;
import vn.avq.shop.infra.AiUnavailableException;
import vn.avq.shop.repository.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/** UC20 (Hình 3.2): gọi AI, áp ngưỡng 0,6, dự phòng bảng size khi AI lỗi hoặc kém tin cậy. */
@Service
public class SizeAdvisorService {
    private final AiServiceClient ai;
    private final SizeChartRepository chartRepo;
    private final BodyProfileRepository profileRepo;
    private final FallbackPolicy fallback;
    private final Clock clock;

    public SizeAdvisorService(AiServiceClient ai, SizeChartRepository chartRepo, BodyProfileRepository profileRepo,
                              FallbackPolicy fallback, Clock clock) {
        this.ai = ai; this.chartRepo = chartRepo; this.profileRepo = profileRepo; this.fallback = fallback; this.clock = clock;
    }

    public SizeAdviceDto advise(Long productId, Long customerId, BodyInput body) {
        List<SizeChart> chart = chartRepo.findByProductIdOrderByHeightMin(productId);
        if (chart.isEmpty()) return SizeAdviceDto.contactShop();                                    // AX-03
        if (customerId != null && body.saveProfile()) {                                            // ANFR-06
            BodyProfile p = profileRepo.findById(customerId).orElseGet(() -> new BodyProfile(customerId, LocalDateTime.now(clock)));
            p.update(body.heightCm(), body.weightKg(), body.fitPref());
            profileRepo.save(p);
        }
        FitType fit = chart.get(0).getProduct().getFitType();
        try {
            AiSizeResult r = ai.predictSize(new SizeFeatures(body.heightCm(), body.weightKg(), body.fitPref().name(), fit.name(),
                    chart.stream().map(SizeChart::getSize).toList(),
                    chart.stream().map(c -> new int[]{c.getHeightMin(), c.getHeightMax()}).toList(), List.of()));
            if (fallback.accept(r)) return SizeAdviceDto.ai(r.size(), r.confidence(), r.reason());
        } catch (AiUnavailableException e) {
            // AX-02: AI lỗi hoặc quá 1,5 giây -> dùng bảng size
        }
        return SizeAdviceDto.byChart(fallback.ruleBasedSize(chart, body, fit));                   // FB-01
    }
}
