package com.ecommerce.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Contrato de la primitiva de refresco: token opaco de 256 bits codificado en Base64 URL y hash
 * SHA-256 estable. Es la base de la seguridad del refresh, así que se prueba directamente (no solo
 * a través de los tests de integración).
 */
class SecureRandomRefreshTokenProviderTest {

    private final SecureRandomRefreshTokenProvider provider = new SecureRandomRefreshTokenProvider();

    @Test
    void generateProducesAUrlSafe256BitToken() {
        // 32 bytes -> 43 caracteres Base64 URL sin padding.
        assertThat(provider.generate()).matches("[A-Za-z0-9_-]{43}");
    }

    @Test
    void generateDoesNotRepeat() {
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            tokens.add(provider.generate());
        }

        assertThat(tokens).hasSize(100);
    }

    @Test
    void hashIsAStableSha256Hex() {
        // Vector conocido: SHA-256("abc").
        assertThat(provider.hash("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void hashOfAGeneratedTokenIsSixtyFourLowercaseHexChars() {
        assertThat(provider.hash(provider.generate())).matches("[0-9a-f]{64}");
    }
}
