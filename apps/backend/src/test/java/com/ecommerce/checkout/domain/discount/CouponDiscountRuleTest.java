package com.ecommerce.checkout.domain.discount;

import static org.assertj.core.api.Assertions.assertThat;

import com.ecommerce.catalog.domain.Category;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CouponDiscountRuleTest {

    private final CouponDiscountRule rule = new CouponDiscountRule();

    private static DiscountContext context(Cart cart, String runningTotal, Optional<AppliedCoupon> coupon) {
        return new DiscountContext(cart, cart.subtotal(), new BigDecimal(runningTotal), coupon);
    }

    @Test
    void appliesTheCouponPercentOverTheRunningTotal() {
        Cart cart = DiscountFixtures.single(Category.HOME, "100.00");

        DiscountStep step =
                rule.apply(context(cart, "115.90", Optional.of(DiscountFixtures.coupon("15")))).orElseThrow();

        assertThat(step.type()).isEqualTo(DiscountStepType.COUPON);
        assertThat(step.label()).isEqualTo("Cupón WELCOME2026");
        assertThat(step.percent()).isEqualByComparingTo("15.00");
        assertThat(step.amount()).isEqualByComparingTo("17.39");
    }

    @Test
    void doesNothingWithoutACoupon() {
        Cart cart = DiscountFixtures.single(Category.HOME, "100.00");

        assertThat(rule.apply(context(cart, "115.90", Optional.empty()))).isEmpty();
    }
}
