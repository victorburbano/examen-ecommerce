package com.ecommerce.checkout.domain.discount;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * Estado que recibe cada regla de la cadena: el carrito, el subtotal original (para el tope), el
 * total corriente (tras las reglas ya aplicadas) y el cupón, si lo hay.
 */
public record DiscountContext(
        Cart cart, BigDecimal originalSubtotal, BigDecimal runningTotal, Optional<AppliedCoupon> coupon) {

    public DiscountContext {
        Objects.requireNonNull(cart, "cart must not be null");
        Objects.requireNonNull(originalSubtotal, "originalSubtotal must not be null");
        Objects.requireNonNull(runningTotal, "runningTotal must not be null");
        Objects.requireNonNull(coupon, "coupon must not be null");
    }

    public DiscountContext withRunningTotal(BigDecimal newRunningTotal) {
        return new DiscountContext(cart, originalSubtotal, newRunningTotal, coupon);
    }
}
