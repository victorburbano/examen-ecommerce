package com.ecommerce.checkout.api;

import com.ecommerce.catalog.domain.Category;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;

/** Línea del desglose tal como la devuelve la API (precios como string decimal exacto). */
public record ItemResponse(
        Long productId,
        String name,
        Category category,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal unitPrice,
        int quantity,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal lineTotal) {
}
