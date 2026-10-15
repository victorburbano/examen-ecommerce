package com.ecommerce.checkout.api;

import com.ecommerce.checkout.domain.discount.DiscountStepType;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;

/**
 * Paso del desglose en la API. {@code percent} es {@code null} en el ajuste por tope
 * ({@code CAP_ADJUSTMENT}); su {@code amount} es negativo para que la suma cuadre.
 */
public record DiscountResponse(
        DiscountStepType type,
        String label,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal percent,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal amount) {
}
