package com.ecommerce.checkout.domain;

import com.ecommerce.catalog.domain.Category;
import com.ecommerce.checkout.domain.discount.CartLine;
import com.ecommerce.checkout.domain.discount.Money;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Línea de una orden: copia del producto en el momento de la compra. Se guarda el nombre y el
 * precio unitario para que la orden siga siendo válida aunque el catálogo cambie.
 */
public record OrderItem(
        Long productId, String name, Category category, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {

    public OrderItem {
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(unitPrice, "unitPrice must not be null");
        Objects.requireNonNull(lineTotal, "lineTotal must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        unitPrice = Money.round(unitPrice);
        lineTotal = Money.round(lineTotal);
    }

    public static OrderItem fromLine(CartLine line) {
        return new OrderItem(
                line.productId(), line.name(), line.category(), line.unitPrice(), line.quantity(), line.lineTotal());
    }
}
