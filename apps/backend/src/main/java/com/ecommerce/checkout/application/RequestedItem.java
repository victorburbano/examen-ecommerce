package com.ecommerce.checkout.application;

import com.ecommerce.checkout.domain.discount.exception.InvalidCartException;

/**
 * Ítem pedido por el cliente: solo producto y cantidad. El cliente nunca envía precios ni
 * totales (los lee el servidor de la base de datos).
 */
public record RequestedItem(Long productId, int quantity) {

    public RequestedItem {
        if (productId == null) {
            throw new InvalidCartException("cart line productId must not be null");
        }
        if (quantity <= 0) {
            throw new InvalidCartException("cart line quantity must be positive");
        }
    }
}
