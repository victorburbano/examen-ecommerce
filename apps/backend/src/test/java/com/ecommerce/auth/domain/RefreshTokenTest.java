package com.ecommerce.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant EXPIRES = NOW.plusSeconds(3600);

    @Test
    void issueCreatesAnActiveTokenOfAFamily() {
        RefreshToken token = RefreshToken.issue("hash", "family", 1L, NOW, EXPIRES);

        assertThat(token.id()).isNull();
        assertThat(token.revokedAt()).isNull();
        assertThat(token.replacedBy()).isNull();
        assertThat(token.isRevoked()).isFalse();
        assertThat(token.isExpiredAt(NOW)).isFalse();
    }

    @Test
    void isExpiredAtMatchesTheBoundaryInstant() {
        RefreshToken token = RefreshToken.issue("hash", "family", 1L, NOW, EXPIRES);

        assertThat(token.isExpiredAt(EXPIRES)).isTrue();
        assertThat(token.isExpiredAt(EXPIRES.minusMillis(1))).isFalse();
    }

    @Test
    void withIdKeepsTheRestOfTheState() {
        RefreshToken token = RefreshToken.issue("hash", "family", 1L, NOW, EXPIRES).withId(9L);

        assertThat(token.id()).isEqualTo(9L);
        assertThat(token.tokenHash()).isEqualTo("hash");
        assertThat(token.createdAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsNullComponents() {
        assertThatThrownBy(() -> new RefreshToken(1L, null, "family", 1L, NOW, EXPIRES, null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new RefreshToken(1L, "hash", null, 1L, NOW, EXPIRES, null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new RefreshToken(1L, "hash", "family", null, NOW, EXPIRES, null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new RefreshToken(1L, "hash", "family", 1L, null, EXPIRES, null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new RefreshToken(1L, "hash", "family", 1L, NOW, null, null, null))
                .isInstanceOf(NullPointerException.class);
    }
}
