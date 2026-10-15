package com.ecommerce.checkout.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * Cuerpo común de {@code quote} y {@code orders}: solo productos y cantidades, más un cupón
 * opcional. Un {@code items} nulo o vacío se trata como carrito vacío (400 EMPTY_CART).
 */
public record CheckoutRequest(List<@Valid ItemRequest> items, String couponCode) {

    /** Ítem pedido: producto y cantidad. El servidor pone los precios. */
    public record ItemRequest(
            @NotNull @Positive Long productId,
            @Positive int quantity) {
    }
}
