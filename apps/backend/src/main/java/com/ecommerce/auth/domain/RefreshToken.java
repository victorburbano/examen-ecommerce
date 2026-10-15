package com.ecommerce.auth.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Refresh token opaco persistido como hash SHA-256. Pertenece a una {@code familyId}:
 * toda la familia se revoca si se detecta reutilización de un token ya rotado.
 */
public record RefreshToken(
        Long id,
        String tokenHash,
        String familyId,
        Long userId,
        Instant createdAt,
        Instant expiresAt,
        Instant revokedAt,
        String replacedBy) {

    public RefreshToken {
        Objects.requireNonNull(tokenHash, "tokenHash must not be null");
        Objects.requireNonNull(familyId, "familyId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }

    /**
     * Token nuevo de una familia: el primero (login) con {@code familyId} nuevo, o el
     * siguiente tras una rotación reutilizando la familia existente.
     */
    public static RefreshToken issue(
            String tokenHash, String familyId, Long userId, Instant issuedAt, Instant expiresAt) {
        return new RefreshToken(null, tokenHash, familyId, userId, issuedAt, expiresAt, null, null);
    }

    public RefreshToken withId(Long assignedId) {
        return new RefreshToken(assignedId, tokenHash, familyId, userId, createdAt, expiresAt, revokedAt, replacedBy);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpiredAt(Instant now) {
        return !expiresAt.isAfter(now);
    }
}
