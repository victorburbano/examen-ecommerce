package com.ecommerce.checkout.domain.discount.exception;

/** El cupón aplicado trae datos inválidos (código vacío o porcentaje fuera de (0, 100]). */
public class InvalidCouponException extends RuntimeException {

    public InvalidCouponException(String message) {
        super(message);
    }
}
