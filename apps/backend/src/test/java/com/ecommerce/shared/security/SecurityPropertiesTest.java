package com.ecommerce.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class SecurityPropertiesTest {

    private static final String VALID_SECRET = "0123456789abcdef0123456789abcdef";

    @Test
    void exposesDurationsAndSecretBytes() {
        SecurityProperties properties = new SecurityProperties(VALID_SECRET, "examen", 15, 7, true, "http://x");

        assertThat(properties.accessTtl()).isEqualTo(Duration.ofMinutes(15));
        assertThat(properties.refreshTtl()).isEqualTo(Duration.ofDays(7));
        assertThat(properties.jwtSecretBytes()).hasSize(32);
        assertThat(properties.cookieSecure()).isTrue();
    }

    @Test
    void failsFastWhenSecretIsMissing() {
        assertThatThrownBy(() -> new SecurityProperties(null, "examen", 15, 7, false, "http://x"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("jwt-secret");
    }

    @Test
    void failsFastWhenSecretIsTooShort() {
        assertThatThrownBy(() -> new SecurityProperties("short", "examen", 15, 7, false, "http://x"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    void failsFastWhenIssuerIsBlank() {
        assertThatThrownBy(() -> new SecurityProperties(VALID_SECRET, "  ", 15, 7, false, "http://x"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("issuer");
        assertThatThrownBy(() -> new SecurityProperties(VALID_SECRET, null, 15, 7, false, "http://x"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void failsFastWhenTtlsAreNotPositive() {
        assertThatThrownBy(() -> new SecurityProperties(VALID_SECRET, "examen", 0, 7, false, "http://x"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("access-ttl-minutes");
        assertThatThrownBy(() -> new SecurityProperties(VALID_SECRET, "examen", 15, -1, false, "http://x"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("refresh-ttl-days");
    }
}
