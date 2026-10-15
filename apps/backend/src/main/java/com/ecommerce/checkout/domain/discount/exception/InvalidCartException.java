package com.ecommerce.checkout.domain.discount.exception;

/** El carrito trae datos corruptos (línea nula, cantidad no positiva, precio negativo, etc.). */
public class InvalidCartException extends RuntimeException {

    public InvalidCartException(String message) {
        super(message);
    }
}
