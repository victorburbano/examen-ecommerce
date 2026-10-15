package com.ecommerce.checkout.domain.discount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ecommerce.checkout.domain.discount.exception.InvalidCouponException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AppliedCouponTest {

    @Test
    void acceptsAValidCouponAndTrimsTheCode() {
        AppliedCoupon coupon = new AppliedCoupon("  WELCOME2026  ", new BigDecimal("15"));

        assertThat(coupon.code()).isEqualTo("WELCOME2026");
        assertThat(coupon.percent()).isEqualByComparingTo("15");
    }

    @Test
    void acceptsTheExtremesOfTheRange() {
        assertThat(new AppliedCoupon("C", new BigDecimal("1")).percent()).isEqualByComparingTo("1");
        assertThat(new AppliedCoupon("C", new BigDecimal("100")).percent()).isEqualByComparingTo("100");
    }

    @Test
    void rejectsABlankCode() {
        assertThatThrownBy(() -> new AppliedCoupon("  ", new BigDecimal("15")))
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("code");
        assertThatThrownBy(() -> new AppliedCoupon(null, new BigDecimal("15")))
                .isInstanceOf(InvalidCouponException.class);
    }

    @Test
    void rejectsANullPercent() {
        assertThatThrownBy(() -> new AppliedCoupon("WELCOME2026", null))
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("percent");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "100.01", "101"})
    void rejectsAPercentOutsideTheRange(String percent) {
        assertThatThrownBy(() -> new AppliedCoupon("WELCOME2026", new BigDecimal(percent)))
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("(0, 100]");
    }
}
