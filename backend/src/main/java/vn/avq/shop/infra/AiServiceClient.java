package vn.avq.shop.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.avq.shop.dto.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.regex.*;

/** Gọi AI Service (FastAPI) với timeout 1,5 giây (ANFR-01); lỗi được bọc thành AiUnavailableException. */
@Component
public class AiServiceClient {
    static final Duration TIMEOUT = Duration.ofMillis(1500);
    private final String baseUrl;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();

    public AiServiceClient(@Value("${ai.base-url}") String baseUrl) { this.baseUrl = baseUrl; }

    public AiSizeResult predictSize(SizeFeatures f) {
        String body = String.format("{\"height_cm\":%d,\"weight_kg\":%d,\"fit_pref\":\"%s\",\"fit_type\":\"%s\",\"sizes\":[%s]}",
                f.heightCm(), f.weightKg(), f.fitPref(), f.fitType(),
                String.join(",", f.sizes().stream().map(s -> "\"" + s + "\"").toList()));
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + "/size/predict")).timeout(TIMEOUT)
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
        try {
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) throw new AiUnavailableException("HTTP " + res.statusCode(), null);
            return new AiSizeResult(field(res.body(), "size"), Double.parseDouble(field(res.body(), "confidence")),
                    field(res.body(), "reason"), field(res.body(), "model_version"));
        } catch (AiUnavailableException e) { throw e;
        } catch (Exception e) { throw new AiUnavailableException("AI Service không phản hồi", e); }
    }

    private static String field(String json, String name) {
        Matcher m = Pattern.compile("\"" + name + "\"\\s*:\\s*\"?([^\",}]*)").matcher(json);
        return m.find() ? m.group(1) : null;
    }
}
