package com.ecommerce.checkout.api;

import com.ecommerce.checkout.application.CheckoutCommand;
import com.ecommerce.checkout.application.PricedCart;
import com.ecommerce.checkout.application.RequestedItem;
import com.ecommerce.checkout.domain.Order;
import com.ecommerce.checkout.domain.OrderDiscount;
import com.ecommerce.checkout.domain.OrderItem;
import com.ecommerce.checkout.domain.PageResult;
import com.ecommerce.checkout.domain.discount.AppliedCoupon;
import com.ecommerce.checkout.domain.discount.CartLine;
import com.ecommerce.checkout.domain.discount.DiscountBreakdown;
import com.ecommerce.checkout.domain.discount.DiscountStep;
import java.util.List;
import org.springframework.stereotype.Component;

/** Traduce entre el dominio del checkout y los DTOs de la API (sin lógica de negocio). */
@Component
public class CheckoutMapper {

    public CheckoutCommand toCommand(CheckoutRequest request) {
        List<RequestedItem> items = request.items() == null
                ? List.of()
                : request.items().stream()
                        .map(item -> new RequestedItem(item.productId(), item.quantity()))
                        .toList();
        return new CheckoutCommand(items, request.couponCode());
    }

    public BreakdownResponse toBreakdownResponse(PricedCart priced) {
        DiscountBreakdown breakdown = priced.breakdown();
        return new BreakdownResponse(
                priced.cart().lines().stream().map(CheckoutMapper::toItemResponse).toList(),
                breakdown.steps().stream().map(CheckoutMapper::toDiscountResponse).toList(),
                breakdown.originalSubtotal(),
                breakdown.totalDiscount(),
                breakdown.effectiveDiscountPercent(),
                breakdown.total(),
                breakdown.capApplied(),
                priced.coupon().map(AppliedCoupon::code).orElse(null));
    }

    public OrderResponse toOrderResponse(Order order) {
        return new OrderResponse(
                order.id(),
                order.createdAt(),
                order.items().stream().map(CheckoutMapper::toItemResponse).toList(),
                order.discounts().stream().map(CheckoutMapper::toDiscountResponse).toList(),
                order.originalSubtotal(),
                order.totalDiscount(),
                order.effectiveDiscountPercent(),
                order.total(),
                order.capApplied(),
                order.couponCode());
    }

    public PageResponse<OrderResponse> toPageResponse(PageResult<Order> page) {
        return new PageResponse<>(
                page.content().stream().map(this::toOrderResponse).toList(),
                page.number(),
                page.size(),
                page.totalElements(),
                page.totalPages());
    }

    private static ItemResponse toItemResponse(CartLine line) {
        return new ItemResponse(
                line.productId(), line.name(), line.category(), line.unitPrice(), line.quantity(), line.lineTotal());
    }

    private static ItemResponse toItemResponse(OrderItem item) {
        return new ItemResponse(
                item.productId(), item.name(), item.category(), item.unitPrice(), item.quantity(), item.lineTotal());
    }

    private static DiscountResponse toDiscountResponse(DiscountStep step) {
        return new DiscountResponse(step.type(), step.label(), step.percent(), step.amount());
    }

    private static DiscountResponse toDiscountResponse(OrderDiscount discount) {
        return new DiscountResponse(discount.type(), discount.label(), discount.percent(), discount.amount());
    }
}
