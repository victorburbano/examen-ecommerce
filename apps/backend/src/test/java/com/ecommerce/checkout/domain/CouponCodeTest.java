package com.ecommerce.checkout.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Normalización del código de cupón: mayúsculas, sin espacios y vacío = sin cupón. */
class CouponCodeTest {

    @Test
    void normalizesToUppercaseWithoutSurroundingSpaces() {
        assertThat(CouponCode.normalize("  welcome2026  ")).contains("WELCOME2026");
        assertThat(CouponCode.normalize("welcome2026")).contains("WELCOME2026");
    }

    @Test
    void treatsNullAndBlankAsNoCoupon() {
        assertThat(CouponCode.normalize(null)).isEmpty();
        assertThat(CouponCode.normalize("")).isEmpty();
        assertThat(CouponCode.normalize("   ")).isEmpty();
    }
}
