package com.ecommerce.checkout.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.ecommerce.checkout.domain.discount.AppliedCoupon;
import com.ecommerce.checkout.domain.exception.CouponExpiredException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Cupón: vigencia inclusiva en ambos extremos, validación y proyección al motor. */
class CouponTest {

    private static final LocalDate FROM = LocalDate.of(2026, 1, 1);
    private static final LocalDate TO = LocalDate.of(2026, 12, 31);

    @Test
    void isUsableOnTheInclusiveBoundaries() {
        Coupon coupon = new Coupon("WELCOME2026", new BigDecimal("15"), FROM, TO);

        assertThat(coupon.isUsableOn(FROM)).isTrue();
        assertThat(coupon.isUsableOn(TO)).isTrue();
        assertThat(coupon.isUsableOn(LocalDate.of(2026, 6, 15))).isTrue();
    }

    @Test
    void isNotUsableBeforeOrAfterTheValidityWindow() {
        Coupon coupon = new Coupon("WELCOME2026", new BigDecimal("15"), FROM, TO);

        assertThat(coupon.isUsableOn(FROM.minusDays(1))).isFalse();
        assertThat(coupon.isUsableOn(TO.plusDays(1))).isFalse();
    }

    @Test
    void assertUsableOnThrowsWhenOutsideTheWindow() {
        Coupon coupon = new Coupon("EXPIRED2025", new BigDecimal("10"), FROM, TO);

        assertThatCode(() -> coupon.assertUsableOn(FROM)).doesNotThrowAnyException();
        assertThatExceptionOfType(CouponExpiredException.class)
                .isThrownBy(() -> coupon.assertUsableOn(TO.plusDays(1)))
                .withMessageContaining("EXPIRED2025");
    }

    @Test
    void normalizesThePercentToTwoDecimals() {
        Coupon coupon = new Coupon("WELCOME2026", new BigDecimal("15.5"), FROM, TO);

        assertThat(coupon.percent()).isEqualByComparingTo("15.50");
    }

    @Test
    void projectsOntoAnAppliedCouponForTheEngine() {
        Coupon coupon = new Coupon("WELCOME2026", new BigDecimal("15.00"), FROM, TO);

        assertThat(coupon.toAppliedCoupon()).isEqualTo(new AppliedCoupon("WELCOME2026", new BigDecimal("15.00")));
    }

    @Test
    void rejectsInvalidData() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Coupon(null, BigDecimal.TEN, FROM, TO))
                .withMessage("code must not be null");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Coupon("  ", BigDecimal.TEN, FROM, TO))
                .withMessage("code must not be blank");
        assertThatNullPointerException()
                .isThrownBy(() -> new Coupon("C", null, FROM, TO))
                .withMessage("percent must not be null");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Coupon("C", BigDecimal.ZERO, FROM, TO))
                .withMessage("percent must be within (0, 100]");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Coupon("C", new BigDecimal("101"), FROM, TO))
                .withMessage("percent must be within (0, 100]");
        assertThatNullPointerException()
                .isThrownBy(() -> new Coupon("C", BigDecimal.TEN, null, TO))
                .withMessage("validFrom must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new Coupon("C", BigDecimal.TEN, FROM, null))
                .withMessage("validTo must not be null");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Coupon("C", BigDecimal.TEN, TO, FROM))
                .withMessage("validFrom must not be after validTo");
    }
}
