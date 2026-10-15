package com.ecommerce.auth.infrastructure;

import com.ecommerce.auth.domain.RefreshToken;
import com.ecommerce.auth.domain.port.RefreshTokenRepository;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Adaptador del puerto de refresh tokens: mapea el dominio a la entidad JPA. */
@Repository
public class JpaRefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    public JpaRefreshTokenRepositoryAdapter(RefreshTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        RefreshTokenJpaEntity saved = jpaRepository.save(new RefreshTokenJpaEntity(
                token.tokenHash(),
                token.familyId(),
                token.userId(),
                token.createdAt(),
                token.expiresAt(),
                token.revokedAt(),
                token.replacedBy()));
        return toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(JpaRefreshTokenRepositoryAdapter::toDomain);
    }

    @Override
    public boolean markRotated(Long id, String replacedByHash, Instant revokedAt) {
        return jpaRepository.markRotated(id, replacedByHash, revokedAt) == 1;
    }

    @Override
    public int revokeFamily(String familyId, Instant revokedAt) {
        return jpaRepository.revokeFamily(familyId, revokedAt);
    }

    private static RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return new RefreshToken(
                entity.getId(),
                entity.getTokenHash(),
                entity.getFamilyId(),
                entity.getUserId(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getReplacedBy());
    }
}
