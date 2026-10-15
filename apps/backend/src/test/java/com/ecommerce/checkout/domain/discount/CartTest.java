package com.ecommerce.checkout.domain.discount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ecommerce.catalog.domain.Category;
import com.ecommerce.checkout.domain.discount.exception.EmptyCartException;
import com.ecommerce.checkout.domain.discount.exception.InvalidCartException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CartTest {

    @Test
    void sumsTheLineTotalsAsTheOriginalSubtotal() {
        Cart cart = DiscountFixtures.cartOf(
                DiscountFixtures.line(1L, Category.TECHNOLOGY, "59.90", 2),
                DiscountFixtures.line(2L, Category.HOME, "9.99", 3));

        assertThat(cart.subtotal()).isEqualByComparingTo("149.77");
        assertThat(cart.subtotal().scale()).isEqualTo(2);
    }

    @Test
    void rejectsAnEmptyCart() {
        assertThatThrownBy(() -> new Cart(List.of()))
                .isInstanceOf(EmptyCartException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void rejectsANullLineList() {
        assertThatThrownBy(() -> new Cart(null))
                .isInstanceOf(InvalidCartException.class)
                .hasMessageContaining("lines");
    }

    @Test
    void rejectsACartWithANullLine() {
        List<CartLine> lines = new ArrayList<>();
        lines.add(DiscountFixtures.line(Category.HOME, "1.00", 1));
        lines.add(null);

        assertThatThrownBy(() -> new Cart(lines))
                .isInstanceOf(InvalidCartException.class)
                .hasMessageContaining("line");
    }

    @Test
    void isImmutableAndDoesNotAliasTheSourceList() {
        List<CartLine> source = new ArrayList<>();
        source.add(DiscountFixtures.line(Category.HOME, "1.00", 1));

        Cart cart = new Cart(source);
        source.clear();

        assertThat(cart.lines()).hasSize(1);
        assertThatThrownBy(() -> cart.lines().clear()).isInstanceOf(UnsupportedOperationException.class);
    }
}
