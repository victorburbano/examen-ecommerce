package com.ecommerce.catalog.domain.exception;

/**
 * Stock insuficiente para la cantidad pedida. Se lanza desde el dominio al decrementar stock
 * (el checkout de la spec 004 lo traduce a 409).
 */
public class InsufficientStockException extends RuntimeException {

    private final transient Long productId;
    private final int requested;
    private final int available;

    public InsufficientStockException(Long productId, int requested, int available) {
        super("Insufficient stock for product %d: requested %d, available %d"
                .formatted(productId, requested, available));
        this.productId = productId;
        this.requested = requested;
        this.available = available;
    }

    public Long productId() {
        return productId;
    }

    public int requested() {
        return requested;
    }

    public int available() {
        return available;
    }
}
