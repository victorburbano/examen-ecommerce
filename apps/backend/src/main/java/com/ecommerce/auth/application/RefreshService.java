package com.ecommerce.auth.application;

import com.ecommerce.auth.domain.RefreshToken;
import com.ecommerce.auth.domain.User;
import com.ecommerce.auth.domain.exception.InvalidRefreshTokenException;
import com.ecommerce.auth.domain.port.AccessTokenIssuer;
import com.ecommerce.auth.domain.port.RefreshTokenProvider;
import com.ecommerce.auth.domain.port.RefreshTokenRepository;
import com.ecommerce.auth.domain.port.UserRepository;
import com.ecommerce.shared.security.SecurityProperties;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Refresh con rotación. Un token solo puede consumirse una vez: {@code markRotated} es una
 * actualización condicional, de modo que si dos peticiones compiten una gana y la otra detecta
 * la reutilización, revoca la familia entera y responde 401.
 */
@Service
public class RefreshService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenProvider refreshTokenProvider;
    private final AccessTokenIssuer accessTokenIssuer;
    private final SecurityProperties securityProperties;
    private final Clock clock;

    public RefreshService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenProvider refreshTokenProvider,
            AccessTokenIssuer accessTokenIssuer,
            SecurityProperties securityProperties,
            Clock clock) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenProvider = refreshTokenProvider;
        this.accessTokenIssuer = accessTokenIssuer;
        this.securityProperties = securityProperties;
        this.clock = clock;
    }

    /**
     * La revocación de la familia debe persistir aunque la petición acabe en 401: por eso esta
     * excepción no provoca rollback de la transacción.
     */
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public AuthResult refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }
        Instant now = clock.instant();
        RefreshToken current = refreshTokenRepository
                .findByTokenHash(refreshTokenProvider.hash(rawRefreshToken))
                .orElseThrow(InvalidRefreshTokenException::new);

        if (current.isRevoked() || current.isExpiredAt(now)) {
            refreshTokenRepository.revokeFamily(current.familyId(), now);
            throw new InvalidRefreshTokenException();
        }

        String nextRawToken = refreshTokenProvider.generate();
        String nextHash = refreshTokenProvider.hash(nextRawToken);
        if (!refreshTokenRepository.markRotated(current.id(), nextHash, now)) {
            // Otra petición rotó este token antes: es una reutilización. Se revoca la familia.
            refreshTokenRepository.revokeFamily(current.familyId(), now);
            throw new InvalidRefreshTokenException();
        }

        refreshTokenRepository.save(RefreshToken.issue(
                nextHash, current.familyId(), current.userId(), now, now.plus(securityProperties.refreshTtl())));

        User user = userRepository.findById(current.userId()).orElseThrow(InvalidRefreshTokenException::new);
        return new AuthResult(user, accessTokenIssuer.issue(user), nextRawToken);
    }
}
