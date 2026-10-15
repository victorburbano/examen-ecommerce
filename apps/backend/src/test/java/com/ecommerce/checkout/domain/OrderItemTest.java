package com.ecommerce.checkout.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.ecommerce.catalog.domain.Category;
import com.ecommerce.checkout.domain.discount.CartLine;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Línea de orden: copia de la línea del carrito y validación. */
class OrderItemTest {

    @Test
    void fromLineCopiesTheCartLine() {
        CartLine line = new CartLine(3L, "Monitor 27\"", Category.TECHNOLOGY, new BigDecimal("249.00"), 2);

        OrderItem item = OrderItem.fromLine(line);

        assertThat(item.productId()).isEqualTo(3L);
        assertThat(item.name()).isEqualTo("Monitor 27\"");
        assertThat(item.category()).isEqualTo(Category.TECHNOLOGY);
        assertThat(item.unitPrice()).isEqualByComparingTo("249.00");
        assertThat(item.quantity()).isEqualTo(2);
        assertThat(item.lineTotal()).isEqualByComparingTo("498.00");
    }

    @Test
    void rejectsInvalidData() {
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderItem(null, "X", Category.HOME, BigDecimal.ONE, 1, BigDecimal.ONE))
                .withMessage("productId must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderItem(1L, null, Category.HOME, BigDecimal.ONE, 1, BigDecimal.ONE))
                .withMessage("name must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderItem(1L, "X", null, BigDecimal.ONE, 1, BigDecimal.ONE))
                .withMessage("category must not be null");
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderItem(1L, "X", Category.HOME, null, 1, BigDecimal.ONE))
                .withMessage("unitPrice must not be null");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new OrderItem(1L, "X", Category.HOME, BigDecimal.ONE, 0, BigDecimal.ONE))
                .withMessage("quantity must be positive");
        assertThatNullPointerException()
                .isThrownBy(() -> new OrderItem(1L, "X", Category.HOME, BigDecimal.ONE, 1, null))
                .withMessage("lineTotal must not be null");
    }
}
