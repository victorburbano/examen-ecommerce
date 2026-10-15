package com.ecommerce.checkout.domain.exception;

/** El cupón indicado no existe en la base de datos (422 COUPON_NOT_FOUND). */
public class CouponNotFoundException extends RuntimeException {

    public CouponNotFoundException(String code) {
        super("Coupon %s not found".formatted(code));
    }
}
