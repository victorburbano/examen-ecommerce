package com.ecommerce.checkout.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.ecommerce.checkout.domain.discount.DiscountStep;
import com.ecommerce.checkout.domain.discount.DiscountStepType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Línea de descuento persistida: porcentaje opcional y validación. */
class OrderDiscountTest {

    @Test
    void fromStepCopiesTheDiscountStep() {
        OrderDiscount discount = OrderDiscount.fromStep(
                new DiscountStep(DiscountStepType.COUPON, "Cupón WELCOME2026 15 %", new BigDecimal("15.00"), new BigDecimal("3.60")));

        assertThat(discount.type()).isEqualTo(DiscountStepType.COUPON);
        assertThat(discount.label()).isEqualTo("Cupón WELCOME2026 15 %");
        assertThat(discount.percent()).isEqualByComparingTo("15.00");
        assertThat(discount.amount()).isEqualByComparingTo("3.60");
    }

    @Test
    void allowsANullPercentForTheCapAdjustment() {
        OrderDiscount discount = new OrderDiscount(
                DiscountStepType.CAP_ADJUSTMENT, "Ajuste por tope (35 %)", null, new BigDecimal("-5.00"));

        assertThat(discount.percent()).isNull();
        assertThat(discount.amount()).isEqualByComparingTo("-5.00");
    }

    @Test
    void rejectsInvalidData() {
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderDiscount(null, "X", null, BigDecimal.ONE))
                .withMessage("type must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderDiscount(DiscountStepType.CATEGORY, null, null, BigDecimal.ONE))
                .withMessage("label must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderDiscount(DiscountStepType.CATEGORY, "X", null, null))
                .withMessage("amount must not be null");
    }
}
