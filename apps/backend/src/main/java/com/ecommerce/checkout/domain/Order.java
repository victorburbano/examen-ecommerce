package com.ecommerce.checkout.domain;

import com.ecommerce.checkout.domain.discount.Cart;
import com.ecommerce.checkout.domain.discount.DiscountBreakdown;
import com.ecommerce.checkout.domain.discount.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Orden de compra: agregado inmutable con las líneas y el desglose congelados. Se persiste el
 * descuento aplicado junto a la orden para poder auditarla sin recalcular nada.
 */
public record Order(
        UUID id,
        Long userId,
        Instant createdAt,
        BigDecimal originalSubtotal,
        BigDecimal totalDiscount,
        BigDecimal total,
        BigDecimal effectiveDiscountPercent,
        boolean capApplied,
        String couponCode,
        List<OrderItem> items,
        List<OrderDiscount> discounts) {

    public Order {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(originalSubtotal, "originalSubtotal must not be null");
        Objects.requireNonNull(totalDiscount, "totalDiscount must not be null");
        Objects.requireNonNull(total, "total must not be null");
        Objects.requireNonNull(effectiveDiscountPercent, "effectiveDiscountPercent must not be null");
        Objects.requireNonNull(items, "items must not be null");
        Objects.requireNonNull(discounts, "discounts must not be null");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("order must have at least one item");
        }
        originalSubtotal = Money.round(originalSubtotal);
        totalDiscount = Money.round(totalDiscount);
        total = Money.round(total);
        effectiveDiscountPercent = Money.round(effectiveDiscountPercent);
        items = List.copyOf(items);
        discounts = List.copyOf(discounts);
    }

    /** Congela el cálculo del motor en una orden nueva (id y fecha los pone el caso de uso). */
    public static Order place(
            Long userId,
            Cart cart,
            DiscountBreakdown breakdown,
            String couponCode,
            UUID id,
            Instant createdAt) {
        List<OrderItem> items = cart.lines().stream().map(OrderItem::fromLine).toList();
        List<OrderDiscount> discounts = breakdown.steps().stream().map(OrderDiscount::fromStep).toList();
        return new Order(
                id,
                userId,
                createdAt,
                breakdown.originalSubtotal(),
                breakdown.totalDiscount(),
                breakdown.total(),
                breakdown.effectiveDiscountPercent(),
                breakdown.capApplied(),
                couponCode,
                items,
                discounts);
    }
}
