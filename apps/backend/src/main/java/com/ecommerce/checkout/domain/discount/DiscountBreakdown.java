package com.ecommerce.checkout.domain.discount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Resultado del motor. Invariante garantizada por {@link DiscountPipeline}: la suma de
 * {@code steps} es {@code totalDiscount} y {@code total = originalSubtotal - totalDiscount}.
 * {@code effectiveDiscountPercent} es el descuento total sobre el subtotal original, en porcentaje.
 */
public record DiscountBreakdown(
        BigDecimal originalSubtotal,
        List<DiscountStep> steps,
        BigDecimal totalDiscount,
        BigDecimal effectiveDiscountPercent,
        BigDecimal total,
        boolean capApplied) {

    public DiscountBreakdown {
        Objects.requireNonNull(originalSubtotal, "originalSubtotal must not be null");
        Objects.requireNonNull(steps, "steps must not be null");
        Objects.requireNonNull(totalDiscount, "totalDiscount must not be null");
        Objects.requireNonNull(effectiveDiscountPercent, "effectiveDiscountPercent must not be null");
        Objects.requireNonNull(total, "total must not be null");
        steps = List.copyOf(steps);
    }
}
