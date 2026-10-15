package com.ecommerce.checkout.domain.discount.exception;

/** El carrito no tiene líneas: no hay nada que cotizar. */
public class EmptyCartException extends RuntimeException {

    public EmptyCartException() {
        super("Cart must not be empty");
    }
}
