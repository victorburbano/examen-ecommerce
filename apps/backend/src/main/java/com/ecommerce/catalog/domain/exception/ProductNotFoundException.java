package com.ecommerce.catalog.domain.exception;

/** Producto inexistente en el catálogo (GET /api/products/{id}). */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long productId) {
        super("Product %d not found".formatted(productId));
    }
}
