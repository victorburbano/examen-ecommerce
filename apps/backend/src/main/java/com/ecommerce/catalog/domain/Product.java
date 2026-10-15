package com.ecommerce.catalog.domain;

import com.ecommerce.catalog.domain.exception.InsufficientStockException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Producto del catálogo. Dominio puro: sin anotaciones de persistencia. El precio es
 * {@link BigDecimal} normalizado a 2 decimales (nunca {@code double}); el id es nulo mientras
 * el producto no está persistido.
 */
public record Product(Long id, String name, Category category, BigDecimal price, int stock) {

    private static final int MONEY_SCALE = 2;

    public Product {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(price, "price must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (price.signum() < 0) {
            throw new IllegalArgumentException("price must not be negative");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("stock must not be negative");
        }
        price = price.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /** ¿Hay stock suficiente para la cantidad pedida? Una cantidad no positiva nunca es válida. */
    public boolean hasStockFor(int quantity) {
        return quantity > 0 && quantity <= stock;
    }

    /** Devuelve el producto con el stock decrementado; si no hay suficiente, lanza excepción. */
    public Product decreaseStock(int quantity) {
        if (!hasStockFor(quantity)) {
            throw new InsufficientStockException(id, quantity, stock);
        }
        return new Product(id, name, category, price, stock - quantity);
    }
}
