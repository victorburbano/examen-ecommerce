package com.ecommerce.checkout.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Página de dominio: total de páginas, validación y página vacía. */
class PageResultTest {

    @Test
    void computesTheTotalNumberOfPagesRoundingUp() {
        assertThat(new PageResult<>(List.of("a", "b"), 0, 10, 45).totalPages()).isEqualTo(5);
        assertThat(new PageResult<>(List.of("a"), 0, 10, 41).totalPages()).isEqualTo(5);
        assertThat(new PageResult<>(List.of("a"), 0, 10, 40).totalPages()).isEqualTo(4);
    }

    @Test
    void anEmptyPageHasNoPages() {
        PageResult<String> empty = PageResult.empty(0, 10);

        assertThat(empty.content()).isEmpty();
        assertThat(empty.totalElements()).isZero();
        assertThat(empty.totalPages()).isZero();
    }

    @Test
    void copiesTheContentImmutably() {
        List<String> source = new java.util.ArrayList<>(List.of("a"));
        PageResult<String> page = new PageResult<>(source, 0, 10, 1);

        source.add("b");

        assertThat(page.content()).containsExactly("a");
    }

    @Test
    void rejectsInvalidArguments() {
        assertThatNullPointerException()
                .isThrownBy(() -> new PageResult<>(null, 0, 10, 0))
                .withMessage("content must not be null");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new PageResult<>(List.of(), -1, 10, 0))
                .withMessage("number must not be negative");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new PageResult<>(List.of(), 0, 0, 0))
                .withMessage("size must be positive");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new PageResult<>(List.of(), 0, 10, -1))
                .withMessage("totalElements must not be negative");
    }
}
