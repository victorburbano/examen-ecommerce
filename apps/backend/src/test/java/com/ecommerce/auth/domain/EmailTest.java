package com.ecommerce.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EmailTest {

    @ParameterizedTest
    @CsvSource({
        "'  User@Example.COM ', user@example.com",
        "user@example.com, user@example.com",
        "ADMIN@EXAMPLE.COM, admin@example.com"
    })
    void normalizesTrimAndLowercase(String raw, String expected) {
        assertThat(Email.normalize(raw)).isEqualTo(expected);
    }

    @Test
    void rejectsNullEmail() {
        assertThatThrownBy(() -> Email.normalize(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email");
    }
}
