package com.ecommerce.checkout.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.ecommerce.checkout.domain.discount.exception.InvalidCartException;
import org.junit.jupiter.api.Test;

/** Ítem pedido: validación defensiva (la validación web ya filtra el caso normal). */
class RequestedItemTest {

    @Test
    void keepsTheProductAndQuantity() {
        RequestedItem item = new RequestedItem(1L, 3);

        assertThat(item.productId()).isEqualTo(1L);
        assertThat(item.quantity()).isEqualTo(3);
    }

    @Test
    void rejectsANullProductId() {
        assertThatExceptionOfType(InvalidCartException.class)
                .isThrownBy(() -> new RequestedItem(null, 1))
                .withMessageContaining("productId must not be null");
    }

    @Test
    void rejectsANonPositiveQuantity() {
        assertThatExceptionOfType(InvalidCartException.class)
                .isThrownBy(() -> new RequestedItem(1L, 0))
                .withMessageContaining("quantity must be positive");
    }
}
