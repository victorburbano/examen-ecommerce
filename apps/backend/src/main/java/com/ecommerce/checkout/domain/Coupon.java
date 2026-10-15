package com.ecommerce.checkout.domain;

import com.ecommerce.checkout.domain.discount.AppliedCoupon;
import com.ecommerce.checkout.domain.discount.Money;
import com.ecommerce.checkout.domain.exception.CouponExpiredException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Cupón de descuento (spec 004). Dominio puro: la vigencia se comprueba con una fecha que
 * inyecta el caso de uso (nunca {@code LocalDate.now()} aquí) y es inclusiva en ambos extremos.
 */
public record Coupon(String code, BigDecimal percent, LocalDate validFrom, LocalDate validTo) {

    public Coupon {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(percent, "percent must not be null");
        Objects.requireNonNull(validFrom, "validFrom must not be null");
        Objects.requireNonNull(validTo, "validTo must not be null");
        if (code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        if (percent.signum() <= 0 || percent.compareTo(Money.HUNDRED) > 0) {
            throw new IllegalArgumentException("percent must be within (0, 100]");
        }
        if (validFrom.isAfter(validTo)) {
            throw new IllegalArgumentException("validFrom must not be after validTo");
        }
        percent = Money.round(percent);
    }

    /** ¿El cupón se puede usar en esa fecha? Vigencia inclusiva en los dos extremos. */
    public boolean isUsableOn(LocalDate date) {
        return !date.isBefore(validFrom) && !date.isAfter(validTo);
    }

    /** Lanza {@link CouponExpiredException} si la fecha cae fuera de la vigencia. */
    public void assertUsableOn(LocalDate date) {
        if (!isUsableOn(date)) {
            throw new CouponExpiredException(code);
        }
    }

    /** Proyección que consume el motor de descuentos: solo código y porcentaje. */
    public AppliedCoupon toAppliedCoupon() {
        return new AppliedCoupon(code, percent);
    }
}
