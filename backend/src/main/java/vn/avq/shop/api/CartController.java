package vn.avq.shop.api;

import org.springframework.web.bind.annotation.*;
import vn.avq.shop.dto.*;
import vn.avq.shop.service.CartService;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {
    private final CartService cartService;
    public CartController(CartService cartService) { this.cartService = cartService; }

    @GetMapping
    public CartDto get(@RequestAttribute("uid") Long customerId) { return cartService.getCart(customerId); }

    @PostMapping("/items")
    public CartDto addItem(@RequestAttribute("uid") Long customerId, @RequestBody AddCartItemRequest req) {
        return cartService.addToCart(customerId, req.variantId(), req.quantity());
    }

    @PatchMapping("/items/{variantId}")
    public CartDto changeQty(@RequestAttribute("uid") Long customerId, @PathVariable Long variantId, @RequestParam int qty) {
        return cartService.changeQty(customerId, variantId, qty);
    }

    @DeleteMapping("/items/{variantId}")
    public CartDto remove(@RequestAttribute("uid") Long customerId, @PathVariable Long variantId) {
        return cartService.remove(customerId, variantId);
    }
}
