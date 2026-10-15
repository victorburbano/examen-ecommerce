package com.ecommerce.checkout.domain.discount;

import com.ecommerce.catalog.domain.Category;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** Fixtures de test: valores de {@code app.discounts} y constructores de carrito legibles. */
final class DiscountFixtures {

    static final Category DISCOUNTED = Category.TECHNOLOGY;

    private DiscountFixtures() {
    }

    /** Valores por defecto de {@code application.yml}: 10 / 100.00 / 5 / 35. */
    static DiscountProperties defaults() {
        return with("10", "100.00", "5", "35");
    }

    static DiscountProperties with(
            String categoryPercent, String volumeThreshold, String volumePercent, String maxTotalPercent) {
        return new DiscountProperties(
                DISCOUNTED,
                new BigDecimal(categoryPercent),
                new BigDecimal(volumeThreshold),
                new BigDecimal(volumePercent),
                new BigDecimal(maxTotalPercent));
    }

    static CartLine line(long productId, Category category, String unitPrice, int quantity) {
        return new CartLine(productId, "Product " + productId, category, new BigDecimal(unitPrice), quantity);
    }

    static CartLine line(Category category, String unitPrice, int quantity) {
        return line(1L, category, unitPrice, quantity);
    }

    static Cart cartOf(CartLine... lines) {
        return new Cart(List.of(lines));
    }

    /** Carrito de una sola línea: la categoría decide si entra en la regla de categoría. */
    static Cart single(Category category, String amount) {
        return cartOf(line(category, amount, 1));
    }

    static AppliedCoupon coupon(String percent) {
        return new AppliedCoupon("WELCOME2026", new BigDecimal(percent));
    }

    static DiscountPipeline pipeline(Optional<AppliedCoupon> coupon) {
        return new DiscountPipelineFactory(defaults()).create(coupon);
    }

    static DiscountPipeline pipeline() {
        return pipeline(Optional.empty());
    }
}
