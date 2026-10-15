package com.ecommerce.checkout.domain.discount;

import com.ecommerce.catalog.domain.Category;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * Regla 1: 10 % (configurable) **solo** sobre las líneas de la categoría configurada. La base es
 * el subtotal de esos productos, no el del carrito entero.
 */
public final class CategoryDiscountRule implements DiscountRule {

    static final String LABEL = "Descuento de categoría";

    private final Category category;
    private final BigDecimal percent;

    public CategoryDiscountRule(DiscountProperties properties) {
        this.category = properties.category();
        this.percent = properties.categoryPercent();
    }

    @Override
    public Optional<DiscountStep> apply(DiscountContext context) {
        BigDecimal categorySubtotal = context.cart().lines().stream()
                .filter(line -> line.category() == category)
                .map(CartLine::lineTotal)
                .reduce(Money.zero(), BigDecimal::add);
        if (categorySubtotal.signum() == 0) {
            return Optional.empty();
        }
        return Optional.of(new DiscountStep(
                DiscountStepType.CATEGORY, LABEL, percent, Money.percentOf(categorySubtotal, percent)));
    }
}
