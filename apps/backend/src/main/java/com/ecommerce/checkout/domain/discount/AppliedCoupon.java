package com.ecommerce.checkout.domain.discount;

import com.ecommerce.checkout.domain.discount.exception.InvalidCouponException;
import java.math.BigDecimal;

/**
 * Cupón ya validado contra la BD: el motor solo necesita su código (para la etiqueta del desglose)
 * y su porcentaje. La vigencia y la existencia se comprueban fuera del motor (spec 004).
 */
public record AppliedCoupon(String code, BigDecimal percent) {

    public AppliedCoupon {
        if (code == null || code.isBlank()) {
            throw new InvalidCouponException("coupon code must not be blank");
        }
        if (percent == null) {
            throw new InvalidCouponException("coupon percent must not be null");
        }
        if (percent.signum() <= 0 || percent.compareTo(Money.HUNDRED) > 0) {
            throw new InvalidCouponException("coupon percent must be within (0, 100]");
        }
        code = code.trim();
    }
}
