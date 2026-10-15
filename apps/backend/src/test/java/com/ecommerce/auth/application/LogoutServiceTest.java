package com.ecommerce.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.auth.domain.RefreshToken;
import com.ecommerce.auth.domain.port.RefreshTokenProvider;
import com.ecommerce.auth.domain.port.RefreshTokenRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RefreshTokenProvider refreshTokenProvider;

    private LogoutService service;

    @BeforeEach
    void setUp() {
        service = new LogoutService(refreshTokenRepository, refreshTokenProvider, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void revokesTheFamilyOfThePresentedToken() {
        RefreshToken token = new RefreshToken(1L, "hash", "family-1", 5L, NOW, NOW.plusSeconds(60), null, null);
        when(refreshTokenProvider.hash("raw")).thenReturn("hash");
        when(refreshTokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(token));

        service.logout("raw");

        verify(refreshTokenRepository).revokeFamily("family-1", NOW);
    }

    @Test
    void isIdempotentWhenThereIsNoCookie() {
        service.logout(null);
        service.logout("   ");

        verify(refreshTokenRepository, never()).findByTokenHash(any());
    }

    @Test
    void isIdempotentWhenTheTokenIsUnknown() {
        when(refreshTokenProvider.hash("raw")).thenReturn("hash");
        when(refreshTokenRepository.findByTokenHash("hash")).thenReturn(Optional.empty());

        service.logout("raw");

        verify(refreshTokenRepository, never()).revokeFamily(any(), any());
        assertThat(true).isTrue();
    }
}
