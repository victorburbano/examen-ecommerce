package com.ecommerce.checkout.domain.discount;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Línea del desglose. {@code amount} es lo que se resta al total (positivo salvo en
 * {@link DiscountStepType#CAP_ADJUSTMENT}, que es negativo para devolver el exceso). {@code percent}
 * es el porcentaje aplicado y es {@code null} solo en el ajuste por tope, que no es una tasa.
 */
public record DiscountStep(DiscountStepType type, String label, BigDecimal percent, BigDecimal amount) {

    public DiscountStep {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(label, "label must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        amount = Money.round(amount);
        if (percent != null) {
            percent = Money.round(percent);
        }
    }
}
