package com.ecommerce.checkout.api;

import java.util.List;

/** Página de la API para el listado de órdenes. */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
}
