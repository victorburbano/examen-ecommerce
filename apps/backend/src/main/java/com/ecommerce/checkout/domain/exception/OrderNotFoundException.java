package com.ecommerce.checkout.domain.exception;

import java.util.UUID;

/**
 * Orden inexistente o no visible para quien pregunta (404 ORDER_NOT_FOUND). Se usa también
 * cuando la orden es de otro {@code USER}: nunca se revela su existencia (anti-IDOR).
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(UUID orderId) {
        super("Order %s not found".formatted(orderId));
    }
}
