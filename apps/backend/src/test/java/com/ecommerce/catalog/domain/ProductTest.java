package com.ecommerce.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ecommerce.catalog.domain.exception.InsufficientStockException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ProductTest {

    private static final Product PRODUCT =
            new Product(1L, "Auriculares Bluetooth", Category.TECHNOLOGY, new BigDecimal("59.90"), 10);

    @Test
    void normalizesThePriceToTwoDecimals() {
        Product product =
                new Product(1L, "Cargador", Category.TECHNOLOGY, new BigDecimal("19.9"), 5);

        assertThat(product.price()).isEqualByComparingTo("19.90");
        assertThat(product.price().scale()).isEqualTo(2);
    }

    @Test
    void roundsHalfUpWhenThePriceHasMoreDecimals() {
        Product product =
                new Product(1L, "Cargador", Category.TECHNOLOGY, new BigDecimal("19.905"), 5);

        assertThat(product.price()).isEqualByComparingTo("19.91");
    }

    @Test
    void rejectsInvalidInvariants() {
        assertThatThrownBy(() -> new Product(1L, " ", Category.HOME, new BigDecimal("1.00"), 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Product(1L, "X", Category.HOME, new BigDecimal("-1.00"), 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Product(1L, "X", Category.HOME, new BigDecimal("1.00"), -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Product(1L, null, Category.HOME, new BigDecimal("1.00"), 1))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Product(1L, "X", null, new BigDecimal("1.00"), 1))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Product(1L, "X", Category.HOME, null, 1))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void hasStockOnlyForPositiveQuantitiesWithinTheAvailableStock() {
        assertThat(PRODUCT.hasStockFor(1)).isTrue();
        assertThat(PRODUCT.hasStockFor(10)).isTrue();
        assertThat(PRODUCT.hasStockFor(11)).isFalse();
        assertThat(PRODUCT.hasStockFor(0)).isFalse();
        assertThat(PRODUCT.hasStockFor(-1)).isFalse();
    }

    @Test
    void decreaseStockReturnsANewProductAndKeepsTheOriginalUntouched() {
        Product decreased = PRODUCT.decreaseStock(4);

        assertThat(decreased.stock()).isEqualTo(6);
        assertThat(PRODUCT.stock()).isEqualTo(10);
        assertThat(decreased).usingRecursiveComparison().ignoringFields("stock").isEqualTo(PRODUCT);
    }

    @Test
    void decreaseStockAllowsLeavingTheStockAtZero() {
        assertThat(PRODUCT.decreaseStock(10).stock()).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {11, 100})
    void decreaseStockFailsWhenThereIsNotEnoughStock(int quantity) {
        assertThatThrownBy(() -> PRODUCT.decreaseStock(quantity))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -3})
    void decreaseStockRejectsNonPositiveQuantities(int quantity) {
        assertThatThrownBy(() -> PRODUCT.decreaseStock(quantity))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void insufficientStockExceptionExposesTheNumbers() {
        InsufficientStockException exception = new InsufficientStockException(8L, 2, 0);

        assertThat(exception.productId()).isEqualTo(8L);
        assertThat(exception.requested()).isEqualTo(2);
        assertThat(exception.available()).isZero();
    }
}
