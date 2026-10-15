package com.ecommerce.checkout.domain.discount;

import java.util.Optional;

/**
 * Regla 3: el porcentaje del cupón sobre el total corriente (cascada, nunca suma de porcentajes).
 * Si no hay cupón no produce línea.
 */
public final class CouponDiscountRule implements DiscountRule {

    @Override
    public Optional<DiscountStep> apply(DiscountContext context) {
        return context.coupon().map(coupon -> new DiscountStep(
                DiscountStepType.COUPON,
                "Cupón " + coupon.code(),
                coupon.percent(),
                Money.percentOf(context.runningTotal(), coupon.percent())));
    }
}
