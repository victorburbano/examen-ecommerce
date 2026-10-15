package com.ecommerce.checkout.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Orden creada: identificador, fecha y el desglose completo (los mismos campos que
 * {@link BreakdownResponse}, aplanados).
 */
public record OrderResponse(
        UUID orderId,
        Instant createdAt,
        List<ItemResponse> items,
        List<DiscountResponse> discounts,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal originalSubtotal,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal totalDiscount,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal effectiveDiscountPercent,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal total,
        boolean capApplied,
        String couponCode) {
}
