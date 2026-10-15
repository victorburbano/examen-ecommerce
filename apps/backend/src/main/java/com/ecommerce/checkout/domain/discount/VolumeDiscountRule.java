package com.ecommerce.checkout.domain.discount;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Regla 2: 5 % (configurable) sobre **todo** el total corriente si este supera estrictamente el
 * umbral (100.00 no califica; 100.01 sí).
 */
public final class VolumeDiscountRule implements DiscountRule {

    static final String LABEL = "Descuento por volumen";

    private final BigDecimal threshold;
    private final BigDecimal percent;

    public VolumeDiscountRule(DiscountProperties properties) {
        this.threshold = properties.volumeThreshold();
        this.percent = properties.volumePercent();
    }

    @Override
    public Optional<DiscountStep> apply(DiscountContext context) {
        if (context.runningTotal().compareTo(threshold) <= 0) {
            return Optional.empty();
        }
        return Optional.of(new DiscountStep(
                DiscountStepType.VOLUME, LABEL, percent, Money.percentOf(context.runningTotal(), percent)));
    }
}
