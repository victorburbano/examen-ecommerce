package com.ecommerce.auth.application;

import com.ecommerce.auth.domain.port.RefreshTokenProvider;
import com.ecommerce.auth.domain.port.RefreshTokenRepository;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Logout idempotente: revoca la familia del refresh presentado (o no hace nada si no hay cookie). */
@Service
public class LogoutService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenProvider refreshTokenProvider;
    private final Clock clock;

    public LogoutService(
            RefreshTokenRepository refreshTokenRepository, RefreshTokenProvider refreshTokenProvider, Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenProvider = refreshTokenProvider;
        this.clock = clock;
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository
                .findByTokenHash(refreshTokenProvider.hash(rawRefreshToken))
                .ifPresent(token -> refreshTokenRepository.revokeFamily(token.familyId(), clock.instant()));
    }
}
