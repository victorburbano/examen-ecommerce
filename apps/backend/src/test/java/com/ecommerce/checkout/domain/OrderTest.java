package com.ecommerce.checkout.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.ecommerce.catalog.domain.Category;
import com.ecommerce.checkout.domain.discount.Cart;
import com.ecommerce.checkout.domain.discount.CartLine;
import com.ecommerce.checkout.domain.discount.DiscountBreakdown;
import com.ecommerce.checkout.domain.discount.DiscountStep;
import com.ecommerce.checkout.domain.discount.DiscountStepType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Orden: congela el cálculo, copia las líneas y normaliza los importes. */
class OrderTest {

    private static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final Instant CREATED_AT = Instant.parse("2026-06-15T10:15:30Z");

    private static Cart cart() {
        return new Cart(List.of(
                new CartLine(1L, "Auriculares Bluetooth", Category.TECHNOLOGY, new BigDecimal("59.90"), 2)));
    }

    private static DiscountBreakdown breakdown() {
        return new DiscountBreakdown(
                new BigDecimal("119.80"),
                List.of(new DiscountStep(DiscountStepType.CATEGORY, "Tecnología 10 %", new BigDecimal("10.00"), new BigDecimal("11.98"))),
                new BigDecimal("11.98"),
                new BigDecimal("10.00"),
                new BigDecimal("107.82"),
                false);
    }

    @Test
    void placeFreezesTheBreakdownAndCopiesTheLines() {
        Order order = Order.place(7L, cart(), breakdown(), "WELCOME2026", ORDER_ID, CREATED_AT);

        assertThat(order.id()).isEqualTo(ORDER_ID);
        assertThat(order.userId()).isEqualTo(7L);
        assertThat(order.createdAt()).isEqualTo(CREATED_AT);
        assertThat(order.originalSubtotal()).isEqualByComparingTo("119.80");
        assertThat(order.totalDiscount()).isEqualByComparingTo("11.98");
        assertThat(order.total()).isEqualByComparingTo("107.82");
        assertThat(order.effectiveDiscountPercent()).isEqualByComparingTo("10.00");
        assertThat(order.capApplied()).isFalse();
        assertThat(order.couponCode()).isEqualTo("WELCOME2026");
        assertThat(order.items()).containsExactly(new OrderItem(
                1L, "Auriculares Bluetooth", Category.TECHNOLOGY, new BigDecimal("59.90"), 2, new BigDecimal("119.80")));
        assertThat(order.discounts()).containsExactly(new OrderDiscount(
                DiscountStepType.CATEGORY, "Tecnología 10 %", new BigDecimal("10.00"), new BigDecimal("11.98")));
    }

    @Test
    void placeAllowsNoCoupon() {
        Order order = Order.place(7L, cart(), breakdown(), null, ORDER_ID, CREATED_AT);

        assertThat(order.couponCode()).isNull();
    }

    @Test
    void copiesTheItemAndDiscountListsImmutably() {
        Order order = Order.place(7L, cart(), breakdown(), null, ORDER_ID, CREATED_AT);

        assertThat(order.items()).isUnmodifiable();
        assertThat(order.discounts()).isUnmodifiable();
    }

    @Test
    void rejectsInvalidData() {
        List<OrderItem> item = List.of(new OrderItem(
                1L, "X", Category.HOME, BigDecimal.ONE, 1, BigDecimal.ONE));
        assertThatNullPointerException()
                .isThrownBy(() -> new Order(null, 1L, CREATED_AT, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE,
                        BigDecimal.ZERO, false, null, item, List.of()))
                .withMessage("id must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new Order(ORDER_ID, null, CREATED_AT, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE,
                        BigDecimal.ZERO, false, null, item, List.of()))
                .withMessage("userId must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new Order(ORDER_ID, 1L, null, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE,
                        BigDecimal.ZERO, false, null, item, List.of()))
                .withMessage("createdAt must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new Order(ORDER_ID, 1L, CREATED_AT, null, BigDecimal.ZERO, BigDecimal.ONE,
                        BigDecimal.ZERO, false, null, item, List.of()))
                .withMessage("originalSubtotal must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new Order(ORDER_ID, 1L, CREATED_AT, BigDecimal.ONE, null, BigDecimal.ONE,
                        BigDecimal.ZERO, false, null, item, List.of()))
                .withMessage("totalDiscount must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new Order(ORDER_ID, 1L, CREATED_AT, BigDecimal.ONE, BigDecimal.ZERO, null,
                        BigDecimal.ZERO, false, null, item, List.of()))
                .withMessage("total must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new Order(ORDER_ID, 1L, CREATED_AT, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE,
                        null, false, null, item, List.of()))
                .withMessage("effectiveDiscountPercent must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new Order(ORDER_ID, 1L, CREATED_AT, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE,
                        BigDecimal.ZERO, false, null, null, List.of()))
                .withMessage("items must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new Order(ORDER_ID, 1L, CREATED_AT, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE,
                        BigDecimal.ZERO, false, null, item, null))
                .withMessage("discounts must not be null");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Order(ORDER_ID, 1L, CREATED_AT, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE,
                        BigDecimal.ZERO, false, null, List.of(), List.of()))
                .withMessage("order must have at least one item");
    }
}
