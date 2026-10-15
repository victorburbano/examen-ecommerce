package com.ecommerce.checkout.domain.discount;

import static org.assertj.core.api.Assertions.assertThat;

import com.ecommerce.catalog.domain.Category;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CategoryDiscountRuleTest {

    private final CategoryDiscountRule rule = new CategoryDiscountRule(DiscountFixtures.defaults());

    private static DiscountContext context(Cart cart) {
        return new DiscountContext(cart, cart.subtotal(), cart.subtotal(), Optional.empty());
    }

    @Test
    void appliesOnlyToTheConfiguredCategory() {
        Cart cart = DiscountFixtures.cartOf(
                DiscountFixtures.line(1L, Category.TECHNOLOGY, "80.00", 1),
                DiscountFixtures.line(2L, Category.HOME, "50.00", 1));

        DiscountStep step = rule.apply(context(cart)).orElseThrow();

        assertThat(step.type()).isEqualTo(DiscountStepType.CATEGORY);
        assertThat(step.label()).isEqualTo("Descuento de categoría");
        assertThat(step.percent()).isEqualByComparingTo("10.00");
        assertThat(step.amount()).isEqualByComparingTo("8.00");
    }

    @Test
    void usesTheQuantitiesInTheCategoryBase() {
        Cart cart = DiscountFixtures.cartOf(
                DiscountFixtures.line(1L, Category.TECHNOLOGY, "59.90", 2),
                DiscountFixtures.line(2L, Category.CLOTHING, "79.00", 1));

        assertThat(rule.apply(context(cart)).orElseThrow().amount()).isEqualByComparingTo("11.98");
    }

    @Test
    void doesNotApplyWithoutLinesOfTheCategory() {
        assertThat(rule.apply(context(DiscountFixtures.single(Category.HOME, "50.00")))).isEmpty();
    }

    @Test
    void doesNotApplyWhenTheCategoryLinesAddUpToZero() {
        assertThat(rule.apply(context(DiscountFixtures.single(Category.TECHNOLOGY, "0.00")))).isEmpty();
    }
}
