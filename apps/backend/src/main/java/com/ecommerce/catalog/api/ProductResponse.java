package com.ecommerce.catalog.api;

import com.ecommerce.catalog.domain.Category;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;

/**
 * Producto tal como lo expone la API. El precio viaja como <strong>string</strong> decimal
 * ({@code "59.90"}) para que ningún cliente lo degrade a coma flotante; la categoría va como
 * nombre del enum.
 */
public record ProductResponse(
        Long id,
        String name,
        Category category,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal price,
        int stock) {
}
