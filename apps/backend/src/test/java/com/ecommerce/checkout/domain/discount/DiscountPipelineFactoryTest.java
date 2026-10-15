package com.ecommerce.checkout.domain.discount;

import static org.assertj.core.api.Assertions.assertThat;

import com.ecommerce.catalog.domain.Category;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DiscountPipelineFactoryTest {

    @Test
    void includesTheCouponRuleOnlyWhenACouponIsPresent() {
        Cart cart = DiscountFixtures.single(Category.HOME, "100.00");

        DiscountBreakdown withCoupon =
                DiscountFixtures.pipeline(Optional.of(DiscountFixtures.coupon("15"))).calculate(cart);
        DiscountBreakdown withoutCoupon = DiscountFixtures.pipeline().calculate(cart);

        assertThat(types(withCoupon)).contains(DiscountStepType.COUPON);
        assertThat(types(withoutCoupon)).doesNotContain(DiscountStepType.COUPON);
        assertThat(withoutCoupon.totalDiscount()).isEqualByComparingTo("0.00");
    }

    @Test
    void alwaysRunsTheRulesInTheConfiguredOrder() {
        DiscountBreakdown breakdown = DiscountFixtures.pipeline(Optional.of(DiscountFixtures.coupon("15")))
                .calculate(DiscountFixtures.cartOf(
                        DiscountFixtures.line(1L, Category.TECHNOLOGY, "80.00", 1),
                        DiscountFixtures.line(2L, Category.HOME, "50.00", 1)));

        assertThat(types(breakdown))
                .containsExactly(DiscountStepType.CATEGORY, DiscountStepType.VOLUME, DiscountStepType.COUPON);
    }

    @Test
    void ignoresACouponThatTheFactoryDidNotReceive() {
        DiscountBreakdown breakdown = new DiscountPipelineFactory(DiscountFixtures.defaults())
                .create(Optional.empty())
                .calculate(DiscountFixtures.single(Category.HOME, "200.00"));

        assertThat(types(breakdown)).doesNotContain(DiscountStepType.COUPON);
    }

    private static List<DiscountStepType> types(DiscountBreakdown breakdown) {
        return breakdown.steps().stream().map(DiscountStep::type).toList();
    }
}
