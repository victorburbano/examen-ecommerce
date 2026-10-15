package com.ecommerce.checkout.domain;

import com.ecommerce.checkout.domain.discount.DiscountStep;
import com.ecommerce.checkout.domain.discount.DiscountStepType;
import com.ecommerce.checkout.domain.discount.Money;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Línea del desglose tal como se persiste con la orden. {@code percent} es {@code null} en el
 * ajuste por tope, que no es una tasa.
 */
public record OrderDiscount(DiscountStepType type, String label, BigDecimal percent, BigDecimal amount) {

    public OrderDiscount {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(label, "label must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        amount = Money.round(amount);
        if (percent != null) {
            percent = Money.round(percent);
        }
    }

    public static OrderDiscount fromStep(DiscountStep step) {
        return new OrderDiscount(step.type(), step.label(), step.percent(), step.amount());
    }
}
