package vn.avq.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.avq.shop.common.BusinessException;
import vn.avq.shop.domain.*;
import vn.avq.shop.dto.CartDto;
import vn.avq.shop.repository.*;

@Service
public class CartService {
    private final CartRepository cartRepo;
    private final CustomerRepository customerRepo;
    private final ProductVariantRepository variantRepo;

    public CartService(CartRepository cartRepo, CustomerRepository customerRepo, ProductVariantRepository variantRepo) {
        this.cartRepo = cartRepo; this.customerRepo = customerRepo; this.variantRepo = variantRepo;
    }

    /** UC07, DT-02: kiểm tra biến thể, tồn kho, giới hạn 10 rồi thêm hoặc cộng dồn. */
    @Transactional
    public CartDto addToCart(Long customerId, Long variantId, int qty) {
        if (variantId == null) throw new BusinessException("VARIANT_REQUIRED", "Vui lòng chọn size và màu");      // BR04
        if (qty < 1) throw new BusinessException("VALIDATION_ERROR", "Số lượng phải lớn hơn 0");
        ProductVariant v = variantRepo.findById(variantId).orElseThrow(() -> new BusinessException("NOT_FOUND"));
        Cart cart = cartRepo.findByCustomerId(customerId).orElseGet(() ->
                cartRepo.save(new Cart(customerRepo.findById(customerId).orElseThrow())));
        int newQty = cart.findItem(variantId).map(CartItem::getQuantity).orElse(0) + qty;
        if (newQty > Cart.MAX_QTY_PER_LINE)
            throw new BusinessException("QTY_LIMIT", "Tối đa " + Cart.MAX_QTY_PER_LINE + " sản phẩm mỗi dòng");    // BR05
        if (!v.isAvailable(newQty))
            throw new BusinessException("OUT_OF_STOCK", "Chỉ còn " + v.getStockQty() + " sản phẩm size "
                    + v.getSize() + " màu " + v.getColor());                                                     // EX-02
        cart.addOrIncrease(v, qty);
        cartRepo.save(cart);
        return toDto(cart);
    }

    /** UC08: xem giỏ. */
    @Transactional(readOnly = true)
    public CartDto getCart(Long customerId) {
        return cartRepo.findByCustomerId(customerId).map(CartService::toDto).orElse(new CartDto(0, java.math.BigDecimal.ZERO));
    }

    /** UC08: đổi số lượng một dòng (BR05, kiểm tra tồn kho). */
    @Transactional
    public CartDto changeQty(Long customerId, Long variantId, int qty) {
        Cart cart = cartRepo.findByCustomerId(customerId).orElseThrow(() -> new BusinessException("CART_EMPTY"));
        CartItem item = cart.findItem(variantId).orElseThrow(() -> new BusinessException("NOT_FOUND"));
        if (qty > Cart.MAX_QTY_PER_LINE) throw new BusinessException("QTY_LIMIT", "Tối đa 10 sản phẩm mỗi dòng");
        if (!item.getVariant().isAvailable(qty))
            throw new BusinessException("OUT_OF_STOCK", "Chỉ còn " + item.getVariant().getStockQty() + " sản phẩm");
        item.changeQty(qty);
        return toDto(cartRepo.save(cart));
    }

    @Transactional
    public CartDto remove(Long customerId, Long variantId) {
        Cart cart = cartRepo.findByCustomerId(customerId).orElseThrow(() -> new BusinessException("CART_EMPTY"));
        cart.removeItems(java.util.Set.of(variantId));
        return toDto(cartRepo.save(cart));
    }

    static CartDto toDto(Cart cart) {
        return new CartDto(cart.totalQuantity(), cart.subtotal(), cart.getItems().stream().map(i -> {
            ProductVariant v = i.getVariant(); Product p = v.getProduct();
            return new vn.avq.shop.dto.CartLineDto(v.getId(), p.getId(), p.getName(), v.getSize(), v.getColor(), p.mainImage(),
                    p.getPrice(), i.getQuantity(), v.getStockQty(), i.lineTotal());
        }).toList());
    }
}
