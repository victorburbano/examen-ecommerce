package com.ecommerce.checkout.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.List;

/**
 * Desglose completo de una cotización: líneas, descuentos paso a paso y totales. Todos los
 * importes viajan como string decimal para que ningún cliente los degrade a coma flotante.
 */
public record BreakdownResponse(
        List<ItemResponse> items,
        List<DiscountResponse> discounts,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal originalSubtotal,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal totalDiscount,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal effectiveDiscountPercent,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal total,
        boolean capApplied,
        String couponCode) {
}
