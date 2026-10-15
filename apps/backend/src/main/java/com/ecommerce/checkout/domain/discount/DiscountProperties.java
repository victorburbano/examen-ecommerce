package com.ecommerce.checkout.domain.discount;

import com.ecommerce.catalog.domain.Category;
import java.math.BigDecimal;

/**
 * Parámetros del motor, enlazados desde {@code app.discounts.*}. Es un tipo de **dominio**: la
 * anotación de Spring vive en infraestructura ({@code DiscountSettings}). Valida los rangos al
 * construirse, así que una configuración inválida impide arrancar (DEC-001).
 */
public record DiscountProperties(
        Category category,
        BigDecimal categoryPercent,
        BigDecimal volumeThreshold,
        BigDecimal volumePercent,
        BigDecimal maxTotalPercent) {

    public DiscountProperties {
        if (category == null) {
            throw new IllegalStateException("app.discounts.category must be set");
        }
        categoryPercent = percent(categoryPercent, "app.discounts.category-percent");
        volumePercent = percent(volumePercent, "app.discounts.volume-percent");
        maxTotalPercent = percent(maxTotalPercent, "app.discounts.max-total-percent");
        if (volumeThreshold == null || volumeThreshold.signum() < 0) {
            throw new IllegalStateException("app.discounts.volume-threshold must not be negative");
        }
        volumeThreshold = Money.round(volumeThreshold);
    }

    private static BigDecimal percent(BigDecimal value, String property) {
        if (value == null || value.signum() <= 0 || value.compareTo(Money.HUNDRED) > 0) {
            throw new IllegalStateException(property + " must be within (0, 100]");
        }
        return Money.round(value);
    }
}
