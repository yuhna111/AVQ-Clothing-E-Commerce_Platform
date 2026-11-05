package vn.avq.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vn.avq.shop.infra.ImageStorage;

/** Phục vụ ảnh đã tải lên tại /uploads/** (công khai, như ảnh sản phẩm có sẵn). */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final ImageStorage storage;
    public WebConfig(ImageStorage storage) { this.storage = storage; }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = storage.getDir().toUri().toString();
        if (!location.endsWith("/")) location += "/";   // thiếu "/" cuối thì Spring hiểu là file, không phải thư mục
        registry.addResourceHandler(ImageStorage.URL_PREFIX + "**").addResourceLocations(location);
    }
}
