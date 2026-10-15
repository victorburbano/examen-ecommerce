package com.ecommerce.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ecommerce.auth.domain.AccessToken;
import com.ecommerce.auth.domain.Role;
import com.ecommerce.auth.domain.User;
import com.ecommerce.shared.security.SecurityProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class JwtIssuerTest {

    private static final Instant NOW = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
    private static final SecurityProperties PROPERTIES =
            new SecurityProperties("0123456789abcdef0123456789abcdef", "https://issuer.example.com", 15, 7, false,
                    "http://localhost:4200");

    private final SecretKey secretKey = new SecretKeySpec(PROPERTIES.jwtSecretBytes(), "HmacSHA256");
    private final JwtIssuer issuer = new JwtIssuer(
            NimbusJwtEncoder.withSecretKey(secretKey).algorithm(MacAlgorithm.HS256).build(),
            PROPERTIES,
            Clock.fixed(NOW, ZoneOffset.UTC));
    private final JwtDecoder decoder =
            NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();

    @Test
    void issuesAHs256JwtWithSubjectRolesAndExpiry() {
        AccessToken accessToken = issuer.issue(new User(42L, "admin@example.com", "hash", Role.ADMIN));

        Jwt jwt = decoder.decode(accessToken.value());

        assertThat(jwt.getSubject()).isEqualTo("42");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo(PROPERTIES.issuer());
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ADMIN");
        assertThat(jwt.getIssuedAt()).isEqualTo(NOW);
        assertThat(jwt.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(15)));
        assertThat(accessToken.expiresInSeconds()).isEqualTo(900);
    }

    @Test
    void aTamperedTokenIsRejected() {
        String token = issuer.issue(new User(1L, "user@example.com", "hash", Role.USER)).value();
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");

        assertThatThrownBy(() -> decoder.decode(tampered)).isInstanceOf(JwtException.class);
    }
}
