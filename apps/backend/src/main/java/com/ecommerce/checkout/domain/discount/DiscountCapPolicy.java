package com.ecommerce.checkout.domain.discount;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Regla 4 (tope): única clase que conoce el máximo del 35 %. Si el descuento acumulado supera el
 * tope, devuelve un paso de ajuste con el exceso en **negativo**, de modo que la suma del desglose
 * siga cuadrando con el descuento mostrado.
 */
public final class DiscountCapPolicy {

    static final String LABEL = "Ajuste por tope de ahorro";

    private final BigDecimal maxTotalPercent;

    public DiscountCapPolicy(DiscountProperties properties) {
        this.maxTotalPercent = properties.maxTotalPercent();
    }

    public Optional<DiscountStep> apply(BigDecimal originalSubtotal, BigDecimal totalDiscount) {
        BigDecimal maxDiscount = Money.percentOf(originalSubtotal, maxTotalPercent);
        if (totalDiscount.compareTo(maxDiscount) <= 0) {
            return Optional.empty();
        }
        return Optional.of(new DiscountStep(
                DiscountStepType.CAP_ADJUSTMENT, LABEL, null, maxDiscount.subtract(totalDiscount)));
    }
}
