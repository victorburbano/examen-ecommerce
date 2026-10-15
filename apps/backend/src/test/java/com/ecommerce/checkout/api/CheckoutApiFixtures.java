package com.ecommerce.checkout.api;

import com.ecommerce.catalog.domain.Category;
import com.ecommerce.catalog.domain.Product;
import com.ecommerce.checkout.application.PricedCart;
import com.ecommerce.checkout.domain.Order;
import com.ecommerce.checkout.domain.OrderDiscount;
import com.ecommerce.checkout.domain.OrderItem;
import com.ecommerce.checkout.domain.discount.AppliedCoupon;
import com.ecommerce.checkout.domain.discount.Cart;
import com.ecommerce.checkout.domain.discount.CartLine;
import com.ecommerce.checkout.domain.discount.DiscountBreakdown;
import com.ecommerce.checkout.domain.discount.DiscountStep;
import com.ecommerce.checkout.domain.discount.DiscountStepType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Fixtures de la capa API: un quote y una orden con el mismo carrito (Tecnología 80 + Hogar 50). */
final class CheckoutApiFixtures {

    static final UUID ORDER_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    static final Instant CREATED_AT = Instant.parse("2026-06-15T10:15:30Z");

    private CheckoutApiFixtures() {
    }

    static PricedCart pricedCart(boolean withCoupon) {
        Cart cart = new Cart(List.of(
                new CartLine(1L, "Monitor 27\"", Category.TECHNOLOGY, new BigDecimal("80.00"), 1),
                new CartLine(2L, "Lámpara", Category.HOME, new BigDecimal("50.00"), 1)));
        List<Product> products = List.of(
                new Product(1L, "Monitor 27\"", Category.TECHNOLOGY, new BigDecimal("80.00"), 10),
                new Product(2L, "Lámpara", Category.HOME, new BigDecimal("50.00"), 5));
        List<DiscountStep> steps = List.of(
                new DiscountStep(DiscountStepType.CATEGORY, "Tecnología 10 %", new BigDecimal("10.00"), new BigDecimal("8.00")),
                new DiscountStep(DiscountStepType.VOLUME, "Volumen 5 %", new BigDecimal("5.00"), new BigDecimal("6.10")),
                new DiscountStep(DiscountStepType.COUPON, "Cupón WELCOME2026 15 %", new BigDecimal("15.00"), new BigDecimal("17.39")));
        DiscountBreakdown breakdown = new DiscountBreakdown(
                new BigDecimal("130.00"), steps, new BigDecimal("31.49"), new BigDecimal("24.22"), new BigDecimal("98.51"), false);
        Optional<AppliedCoupon> coupon =
                withCoupon ? Optional.of(new AppliedCoupon("WELCOME2026", new BigDecimal("15.00"))) : Optional.empty();
        return new PricedCart(cart, products, coupon, breakdown);
    }

    static Order order() {
        return new Order(
                ORDER_ID,
                7L,
                CREATED_AT,
                new BigDecimal("130.00"),
                new BigDecimal("31.49"),
                new BigDecimal("98.51"),
                new BigDecimal("24.22"),
                false,
                "WELCOME2026",
                List.of(
                        new OrderItem(1L, "Monitor 27\"", Category.TECHNOLOGY, new BigDecimal("80.00"), 1, new BigDecimal("80.00")),
                        new OrderItem(2L, "Lámpara", Category.HOME, new BigDecimal("50.00"), 1, new BigDecimal("50.00"))),
                List.of(
                        new OrderDiscount(DiscountStepType.CATEGORY, "Tecnología 10 %", new BigDecimal("10.00"), new BigDecimal("8.00")),
                        new OrderDiscount(DiscountStepType.VOLUME, "Volumen 5 %", new BigDecimal("5.00"), new BigDecimal("6.10")),
                        new OrderDiscount(DiscountStepType.COUPON, "Cupón WELCOME2026 15 %", new BigDecimal("15.00"), new BigDecimal("17.39"))));
    }
}
