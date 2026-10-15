package com.ecommerce.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.auth.domain.AccessToken;
import com.ecommerce.auth.domain.RefreshToken;
import com.ecommerce.auth.domain.Role;
import com.ecommerce.auth.domain.User;
import com.ecommerce.auth.domain.exception.InvalidRefreshTokenException;
import com.ecommerce.auth.domain.port.AccessTokenIssuer;
import com.ecommerce.auth.domain.port.RefreshTokenProvider;
import com.ecommerce.auth.domain.port.RefreshTokenRepository;
import com.ecommerce.auth.domain.port.UserRepository;
import com.ecommerce.shared.security.SecurityProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final User USER = new User(5L, "user@example.com", "bcrypt-hash", Role.USER);
    private static final RefreshToken ACTIVE =
            new RefreshToken(1L, "hash-old", "family-1", 5L, NOW.minusSeconds(60), NOW.plusSeconds(3600), null, null);

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RefreshTokenProvider refreshTokenProvider;

    @Mock
    private AccessTokenIssuer accessTokenIssuer;

    private RefreshService service;

    @BeforeEach
    void setUp() {
        SecurityProperties properties =
                new SecurityProperties("0123456789abcdef0123456789abcdef", "examen", 15, 7, false, "http://x");
        service = new RefreshService(
                userRepository,
                refreshTokenRepository,
                refreshTokenProvider,
                accessTokenIssuer,
                properties,
                Clock.fixed(NOW, ZoneOffset.UTC));
        lenient().when(refreshTokenProvider.hash("raw")).thenReturn("hash-old");
    }

    @Test
    void rotatesTheTokenKeepingTheFamilyAndIssuesNewTokens() {
        when(refreshTokenRepository.findByTokenHash("hash-old")).thenReturn(Optional.of(ACTIVE));
        when(refreshTokenProvider.generate()).thenReturn("raw-new");
        when(refreshTokenProvider.hash("raw-new")).thenReturn("hash-new");
        when(refreshTokenRepository.markRotated(1L, "hash-new", NOW)).thenReturn(true);
        when(userRepository.findById(5L)).thenReturn(Optional.of(USER));
        when(accessTokenIssuer.issue(USER)).thenReturn(new AccessToken("jwt-new", 900));

        AuthResult result = service.refresh("raw");

        assertThat(result.refreshToken()).isEqualTo("raw-new");
        assertThat(result.accessToken().value()).isEqualTo("jwt-new");
        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(saved.capture());
        assertThat(saved.getValue().tokenHash()).isEqualTo("hash-new");
        assertThat(saved.getValue().familyId()).isEqualTo("family-1");
        assertThat(saved.getValue().createdAt()).isEqualTo(NOW);
        assertThat(saved.getValue().expiresAt()).isEqualTo(NOW.plus(Duration.ofDays(7)));
    }

    @Test
    void rejectsAnUnknownToken() {
        when(refreshTokenRepository.findByTokenHash("hash-old")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh("raw")).isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository, never()).revokeFamily(any(), any());
    }

    @Test
    void rejectsAnAlreadyRevokedTokenAndRevokesTheWholeFamily() {
        RefreshToken revoked = new RefreshToken(
                1L, "hash-old", "family-1", 5L, NOW.minusSeconds(120), NOW.plusSeconds(3600), NOW.minusSeconds(60), null);
        when(refreshTokenRepository.findByTokenHash("hash-old")).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> service.refresh("raw")).isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository).revokeFamily("family-1", NOW);
        verify(refreshTokenRepository, never()).markRotated(any(), any(), any());
    }

    @Test
    void rejectsAnExpiredTokenAndRevokesTheWholeFamily() {
        RefreshToken expired =
                new RefreshToken(1L, "hash-old", "family-1", 5L, NOW.minusSeconds(7200), NOW.minusSeconds(1), null, null);
        when(refreshTokenRepository.findByTokenHash("hash-old")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.refresh("raw")).isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository).revokeFamily("family-1", NOW);
    }

    @Test
    void revokesTheFamilyWhenAnotherRequestWonTheRace() {
        when(refreshTokenRepository.findByTokenHash("hash-old")).thenReturn(Optional.of(ACTIVE));
        when(refreshTokenProvider.generate()).thenReturn("raw-new");
        when(refreshTokenProvider.hash("raw-new")).thenReturn("hash-new");
        when(refreshTokenRepository.markRotated(1L, "hash-new", NOW)).thenReturn(false);

        assertThatThrownBy(() -> service.refresh("raw")).isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository).revokeFamily("family-1", NOW);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void rejectsTheRotationWhenTheUserNoLongerExists() {
        when(refreshTokenRepository.findByTokenHash("hash-old")).thenReturn(Optional.of(ACTIVE));
        when(refreshTokenProvider.generate()).thenReturn("raw-new");
        when(refreshTokenProvider.hash("raw-new")).thenReturn("hash-new");
        when(refreshTokenRepository.markRotated(1L, "hash-new", NOW)).thenReturn(true);
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh("raw")).isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void rejectsANullTokenWithoutTouchingTheRepository() {
        assertThatThrownBy(() -> service.refresh(null)).isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository, never()).findByTokenHash(any());
    }

    @Test
    void hashesThePresentedTokenToLookItUp() {
        when(refreshTokenRepository.findByTokenHash("hash-old")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh("raw")).isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenProvider).hash(eq("raw"));
    }
}
