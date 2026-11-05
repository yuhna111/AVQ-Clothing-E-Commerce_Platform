package vn.avq.shop.api;

import org.springframework.web.bind.annotation.*;
import vn.avq.shop.dto.AddressDto;
import vn.avq.shop.service.AccountService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
public class AccountController {
    private final AccountService accountService;
    public AccountController(AccountService accountService) { this.accountService = accountService; }

    @GetMapping
    public List<AddressDto> list(@RequestAttribute("uid") Long customerId) { return accountService.addresses(customerId); }

    @PostMapping
    public List<AddressDto> add(@RequestAttribute("uid") Long customerId, @RequestBody AddressDto req) {
        return accountService.addAddress(customerId, req);
    }
}
