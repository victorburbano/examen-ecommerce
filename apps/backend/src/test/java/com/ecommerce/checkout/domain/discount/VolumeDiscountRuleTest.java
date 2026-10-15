package com.ecommerce.checkout.domain.discount;

import static org.assertj.core.api.Assertions.assertThat;

import com.ecommerce.catalog.domain.Category;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VolumeDiscountRuleTest {

    private final VolumeDiscountRule rule = new VolumeDiscountRule(DiscountFixtures.defaults());

    private static DiscountContext context(Cart cart, String runningTotal) {
        return new DiscountContext(cart, cart.subtotal(), new BigDecimal(runningTotal), Optional.empty());
    }

    @Test
    void appliesWhenTheRunningTotalIsStrictlyAboveTheThreshold() {
        Cart cart = DiscountFixtures.single(Category.HOME, "100.01");

        DiscountStep step = rule.apply(context(cart, "100.01")).orElseThrow();

        assertThat(step.type()).isEqualTo(DiscountStepType.VOLUME);
        assertThat(step.label()).isEqualTo("Descuento por volumen");
        assertThat(step.percent()).isEqualByComparingTo("5.00");
        assertThat(step.amount()).isEqualByComparingTo("5.00");
    }

    @Test
    void doesNotApplyExactlyAtTheThreshold() {
        Cart cart = DiscountFixtures.single(Category.HOME, "100.00");

        assertThat(rule.apply(context(cart, "100.00"))).isEmpty();
    }

    @Test
    void doesNotApplyBelowTheThreshold() {
        Cart cart = DiscountFixtures.single(Category.HOME, "90.00");

        assertThat(rule.apply(context(cart, "90.00"))).isEmpty();
    }

    @Test
    void usesTheRunningTotalAndNotTheOriginalSubtotal() {
        Cart cart = DiscountFixtures.single(Category.TECHNOLOGY, "200.00");

        assertThat(rule.apply(context(cart, "90.00"))).isEmpty();
    }

    @Test
    void roundsHalfUpOverTheRunningTotal() {
        Cart cart = DiscountFixtures.single(Category.HOME, "100.10");

        assertThat(rule.apply(context(cart, "100.10")).orElseThrow().amount()).isEqualByComparingTo("5.01");
    }
}
