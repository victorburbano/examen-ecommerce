package com.ecommerce.checkout.domain.discount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ecommerce.catalog.domain.Category;
import com.ecommerce.checkout.domain.discount.exception.InvalidCartException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CartLineTest {

    @Test
    void normalizesTheUnitPriceAndComputesTheLineTotal() {
        CartLine line = new CartLine(1L, "Monitor 27\"", Category.TECHNOLOGY, new BigDecimal("249.005"), 2);

        assertThat(line.unitPrice()).isEqualByComparingTo("249.01");
        assertThat(line.unitPrice().scale()).isEqualTo(2);
        assertThat(line.lineTotal()).isEqualByComparingTo("498.02");
    }

    @Test
    void acceptsAZeroPrice() {
        CartLine line = new CartLine(1L, "Freebie", Category.HOME, BigDecimal.ZERO, 3);

        assertThat(line.lineTotal()).isEqualByComparingTo("0.00");
    }

    @Test
    void rejectsANullProductId() {
        assertThatThrownBy(() -> new CartLine(null, "X", Category.HOME, new BigDecimal("1.00"), 1))
                .isInstanceOf(InvalidCartException.class)
                .hasMessageContaining("productId");
    }

    @Test
    void rejectsABlankName() {
        assertThatThrownBy(() -> new CartLine(1L, "  ", Category.HOME, new BigDecimal("1.00"), 1))
                .isInstanceOf(InvalidCartException.class)
                .hasMessageContaining("name");
        assertThatThrownBy(() -> new CartLine(1L, null, Category.HOME, new BigDecimal("1.00"), 1))
                .isInstanceOf(InvalidCartException.class);
    }

    @Test
    void rejectsANullCategory() {
        assertThatThrownBy(() -> new CartLine(1L, "X", null, new BigDecimal("1.00"), 1))
                .isInstanceOf(InvalidCartException.class)
                .hasMessageContaining("category");
    }

    @Test
    void rejectsANullOrNegativeUnitPrice() {
        assertThatThrownBy(() -> new CartLine(1L, "X", Category.HOME, null, 1))
                .isInstanceOf(InvalidCartException.class)
                .hasMessageContaining("unitPrice");
        assertThatThrownBy(() -> new CartLine(1L, "X", Category.HOME, new BigDecimal("-0.01"), 1))
                .isInstanceOf(InvalidCartException.class)
                .hasMessageContaining("negative");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -3})
    void rejectsNonPositiveQuantities(int quantity) {
        assertThatThrownBy(() -> new CartLine(1L, "X", Category.HOME, new BigDecimal("1.00"), quantity))
                .isInstanceOf(InvalidCartException.class)
                .hasMessageContaining("quantity");
    }
}
