package com.ecommerce.checkout.domain.discount;

import com.ecommerce.checkout.domain.discount.exception.EmptyCartException;
import com.ecommerce.checkout.domain.discount.exception.InvalidCartException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Carrito inmutable que entra al motor. Se valida al construirse: vacío lanza
 * {@link EmptyCartException}; datos corruptos (incluidas líneas nulas) lanzan
 * {@link InvalidCartException}.
 */
public record Cart(List<CartLine> lines) {

    public Cart {
        if (lines == null) {
            throw new InvalidCartException("cart lines must not be null");
        }
        if (lines.isEmpty()) {
            throw new EmptyCartException();
        }
        for (CartLine line : lines) {
            if (line == null) {
                throw new InvalidCartException("cart line must not be null");
            }
        }
        lines = List.copyOf(lines);
    }

    /** Subtotal original: suma de los totales de línea, scale 2. */
    public BigDecimal subtotal() {
        return lines.stream().map(CartLine::lineTotal).reduce(Money.zero(), BigDecimal::add);
    }
}
