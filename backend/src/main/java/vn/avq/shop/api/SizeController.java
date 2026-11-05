package vn.avq.shop.api;

import org.springframework.web.bind.annotation.*;
import vn.avq.shop.dto.*;
import vn.avq.shop.service.SizeAdvisorService;

@RestController
@RequestMapping("/api/v1/products")
public class SizeController {
    private final SizeAdvisorService sizeAdvisor;
    public SizeController(SizeAdvisorService sizeAdvisor) { this.sizeAdvisor = sizeAdvisor; }

    @PostMapping("/{productId}/size-advice")
    public SizeAdviceDto advise(@PathVariable Long productId, @RequestAttribute(name = "uid", required = false) Long customerId,
                                @RequestBody BodyInput body) {
        return sizeAdvisor.advise(productId, customerId, body);
    }
}
