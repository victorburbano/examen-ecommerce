package com.ecommerce.auth.domain.port;

import com.ecommerce.auth.domain.RefreshToken;
import java.time.Instant;
import java.util.Optional;

/** Puerto de persistencia de refresh tokens (familia + rotación). */
public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Marca un token como rotado solo si seguía activo.
     *
     * @return {@code true} si esta llamada ganó la carrera; {@code false} si ya estaba revocado
     *     (rotación concurrente o reutilización).
     */
    boolean markRotated(Long id, String replacedByHash, Instant revokedAt);

    /**
     * Revoca todos los tokens de una familia que sigan activos.
     *
     * @return número de tokens revocados
     */
    int revokeFamily(String familyId, Instant revokedAt);
}
