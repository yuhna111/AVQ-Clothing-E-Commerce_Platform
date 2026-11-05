package vn.avq.shop.service;

import org.junit.jupiter.api.*;
import vn.avq.shop.domain.*;
import vn.avq.shop.dto.*;
import vn.avq.shop.infra.*;
import vn.avq.shop.support.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UC20 Get Size Advice (FB-01, AX-02)")
class SizeAdvisorServiceTest {
    Shop s; Fakes.Charts charts; Fakes.Profiles profiles;
    @BeforeEach void setUp() {
        s = new Shop(); charts = new Fakes.Charts(); profiles = new Fakes.Profiles();
        Product hd = s.hoodieL.getProduct();
        charts.save(new SizeChart(hd, "M", 155, 165, 45, 55)); charts.save(new SizeChart(hd, "L", 165, 175, 55, 68));
        charts.save(new SizeChart(hd, "XL", 175, 185, 68, 80));
    }
    SizeAdvisorService svc(AiServiceClient ai) { return new SizeAdvisorService(ai, Fakes.repo(vn.avq.shop.repository.SizeChartRepository.class, charts), Fakes.repo(vn.avq.shop.repository.BodyProfileRepository.class, profiles), new FallbackPolicy(), s.clock); }
    AiServiceClient aiReturning(String size, double c) {
        return new AiServiceClient("http://ai") { @Override public AiSizeResult predictSize(SizeFeatures f) {
            return new AiSizeResult(size, c, "Phom oversize, nên chọn L thay vì XL", "size-1.2"); } };
    }
    @Test @DisplayName("AI tin cậy 0,82 -> dùng gợi ý AI")
    void aiAccepted() {
        SizeAdviceDto r = svc(aiReturning("L", 0.82)).advise(2L, s.khachA.getId(), new BodyInput(170, 62, FitPref.VUA, true));
        assertEquals("AI", r.source()); assertEquals("L", r.size());
        assertEquals(170, profiles.findById(s.khachA.getId()).orElseThrow().getHeightCm());
    }
    @Test @DisplayName("AI tin cậy 0,41 < 0,6 -> dự phòng bảng size")
    void lowConfidence() {
        SizeAdviceDto r = svc(aiReturning("XL", 0.41)).advise(2L, null, new BodyInput(178, 72, FitPref.RONG, false));
        assertEquals("CHART", r.source()); assertEquals("XL", r.size());
    }
    @Test @DisplayName("AI Service không chạy (kết nối thật bị từ chối) -> bảng size, phom oversize + mặc vừa lùi 1 size")
    void aiDown() {
        SizeAdviceDto r = svc(new AiServiceClient("http://127.0.0.1:9")).advise(2L, null, new BodyInput(170, 62, FitPref.VUA, false));
        assertEquals("CHART", r.source()); assertEquals("M", r.size());
    }
    @Test @DisplayName("Sản phẩm chưa có bảng size -> đề nghị liên hệ (AX-03)")
    void noChart() {
        assertEquals("NONE", svc(aiReturning("L", 0.9)).advise(99L, null, new BodyInput(170, 62, FitPref.VUA, false)).source());
    }
}
