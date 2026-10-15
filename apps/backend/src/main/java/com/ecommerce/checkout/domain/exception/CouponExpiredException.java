package com.ecommerce.checkout.domain.exception;

/** El cupón existe pero está fuera de su vigencia (422 COUPON_EXPIRED). */
public class CouponExpiredException extends RuntimeException {

    public CouponExpiredException(String code) {
        super("Coupon %s is not valid at the current date".formatted(code));
    }
}
