package com.ecommerce.checkout.domain;

import java.util.List;
import java.util.Objects;

/**
 * Página de resultados del dominio: evita que los puertos expongan tipos de Spring Data
 * ({@code Pageable}/{@code Page}) y mantiene el dominio libre de framework.
 */
public record PageResult<T>(List<T> content, int number, int size, long totalElements) {

    public PageResult {
        Objects.requireNonNull(content, "content must not be null");
        if (number < 0) {
            throw new IllegalArgumentException("number must not be negative");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be positive");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("totalElements must not be negative");
        }
        content = List.copyOf(content);
    }

    public int totalPages() {
        return (int) ((totalElements + size - 1) / size);
    }

    public static <T> PageResult<T> empty(int number, int size) {
        return new PageResult<>(List.of(), number, size, 0);
    }
}
