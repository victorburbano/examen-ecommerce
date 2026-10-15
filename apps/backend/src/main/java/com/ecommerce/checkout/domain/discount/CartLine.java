package com.ecommerce.checkout.domain.discount;

import com.ecommerce.catalog.domain.Category;
import com.ecommerce.checkout.domain.discount.exception.InvalidCartException;
import java.math.BigDecimal;

/**
 * Línea del carrito tal como la ve el motor: producto, categoría, precio unitario y cantidad.
 * El precio se normaliza a 2 decimales; la cantidad debe ser positiva.
 */
public record CartLine(Long productId, String name, Category category, BigDecimal unitPrice, int quantity) {

    public CartLine {
        if (productId == null) {
            throw new InvalidCartException("cart line productId must not be null");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidCartException("cart line name must not be blank");
        }
        if (category == null) {
            throw new InvalidCartException("cart line category must not be null");
        }
        if (unitPrice == null) {
            throw new InvalidCartException("cart line unitPrice must not be null");
        }
        if (unitPrice.signum() < 0) {
            throw new InvalidCartException("cart line unitPrice must not be negative");
        }
        if (quantity <= 0) {
            throw new InvalidCartException("cart line quantity must be positive");
        }
        unitPrice = Money.round(unitPrice);
    }

    /** Total de la línea (precio unitario × cantidad), scale 2. */
    public BigDecimal lineTotal() {
        return Money.round(unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }
}
