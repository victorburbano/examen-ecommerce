package com.ecommerce.checkout.domain.discount;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DiscountCapPolicyTest {

    private final DiscountCapPolicy policy = new DiscountCapPolicy(DiscountFixtures.defaults());

    @Test
    void doesNothingWhenTheDiscountIsBelowTheCap() {
        assertThat(policy.apply(new BigDecimal("200.00"), new BigDecimal("54.65"))).isEmpty();
    }

    @Test
    void doesNothingExactlyAtTheCap() {
        assertThat(policy.apply(new BigDecimal("100.00"), new BigDecimal("35.00"))).isEmpty();
    }

    @Test
    void returnsANegativeAdjustmentWhenTheCapIsExceeded() {
        DiscountStep step = policy.apply(new BigDecimal("200.00"), new BigDecimal("97.40")).orElseThrow();

        assertThat(step.type()).isEqualTo(DiscountStepType.CAP_ADJUSTMENT);
        assertThat(step.label()).isEqualTo("Ajuste por tope de ahorro");
        assertThat(step.percent()).isNull();
        assertThat(step.amount()).isEqualByComparingTo("-27.40");
    }

    @Test
    void roundsTheCapOverANonIntegerSubtotal() {
        DiscountStep step = policy.apply(new BigDecimal("100.01"), new BigDecimal("38.25")).orElseThrow();

        assertThat(step.amount()).isEqualByComparingTo("-3.25");
    }

    @Test
    void usesTheConfiguredCap() {
        DiscountCapPolicy lowCap = new DiscountCapPolicy(DiscountFixtures.with("10", "100.00", "5", "10"));

        assertThat(lowCap.apply(new BigDecimal("100.00"), new BigDecimal("23.50")).orElseThrow().amount())
                .isEqualByComparingTo("-13.50");
    }
}
