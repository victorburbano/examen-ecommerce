package com.ecommerce.shared.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de {@code app.security.*}. El secreto JWT solo llega por entorno y se valida al
 * enlazar: si falta o mide menos de 32 bytes, el contexto no arranca.
 */
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        String jwtSecret,
        String issuer,
        long accessTtlMinutes,
        long refreshTtlDays,
        boolean cookieSecure,
        String allowedOrigin) {

    /** HS256 exige una clave de al menos 256 bits (RFC 7518). */
    public static final int MIN_SECRET_BYTES = 32;

    public SecurityProperties {
        if (jwtSecret == null || jwtSecret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "app.security.jwt-secret (JWT_SECRET) must be set and at least " + MIN_SECRET_BYTES + " bytes");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("app.security.issuer must not be blank");
        }
        if (accessTtlMinutes <= 0) {
            throw new IllegalStateException("app.security.access-ttl-minutes must be positive");
        }
        if (refreshTtlDays <= 0) {
            throw new IllegalStateException("app.security.refresh-ttl-days must be positive");
        }
    }

    public byte[] jwtSecretBytes() {
        return jwtSecret.getBytes(StandardCharsets.UTF_8);
    }

    public Duration accessTtl() {
        return Duration.ofMinutes(accessTtlMinutes);
    }

    public Duration refreshTtl() {
        return Duration.ofDays(refreshTtlDays);
    }
}
