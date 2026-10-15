package com.ecommerce.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SecureRandomRefreshTokenProviderTest {

    private final SecureRandomRefreshTokenProvider provider = new SecureRandomRefreshTokenProvider();

    @Test
    void generateReturnsDistinctOpaqueTokens() {
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            tokens.add(provider.generate());
        }

        assertThat(tokens).hasSize(50);
        assertThat(tokens).allSatisfy(token -> assertThat(token).matches("[A-Za-z0-9_-]{43}"));
    }

    @Test
    void hashIsDeterministicHexAndDifferentFromTheRawToken() {
        String rawToken = provider.generate();
        String hash = provider.hash(rawToken);

        assertThat(hash).isEqualTo(provider.hash(rawToken));
        assertThat(hash).hasSize(64).matches("[0-9a-f]{64}");
        assertThat(hash).isNotEqualTo(rawToken);
    }
}
