package com.ecommerce.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.auth.domain.AccessToken;
import com.ecommerce.auth.domain.RefreshToken;
import com.ecommerce.auth.domain.Role;
import com.ecommerce.auth.domain.User;
import com.ecommerce.auth.domain.exception.InvalidCredentialsException;
import com.ecommerce.auth.domain.port.AccessTokenIssuer;
import com.ecommerce.auth.domain.port.PasswordHasher;
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
class LoginServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final User USER = new User(5L, "user@example.com", "bcrypt-hash", Role.USER);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private AccessTokenIssuer accessTokenIssuer;

    @Mock
    private RefreshTokenProvider refreshTokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private LoginService service;

    @BeforeEach
    void setUp() {
        SecurityProperties properties =
                new SecurityProperties("0123456789abcdef0123456789abcdef", "examen", 15, 7, false, "http://x");
        service = new LoginService(
                userRepository,
                passwordHasher,
                accessTokenIssuer,
                refreshTokenProvider,
                refreshTokenRepository,
                properties,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void issuesAnAccessTokenAndAFreshFamilyRefreshToken() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(USER));
        when(passwordHasher.matches("User123!", "bcrypt-hash")).thenReturn(true);
        when(accessTokenIssuer.issue(USER)).thenReturn(new AccessToken("jwt-value", 900));
        when(refreshTokenProvider.generate()).thenReturn("raw-refresh");
        when(refreshTokenProvider.hash("raw-refresh")).thenReturn("hashed-refresh");

        AuthResult result = service.login("  User@Example.com", "User123!");

        assertThat(result.user()).isEqualTo(USER);
        assertThat(result.accessToken().value()).isEqualTo("jwt-value");
        assertThat(result.refreshToken()).isEqualTo("raw-refresh");

        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(saved.capture());
        assertThat(saved.getValue().tokenHash()).isEqualTo("hashed-refresh");
        assertThat(saved.getValue().userId()).isEqualTo(5L);
        assertThat(saved.getValue().familyId()).isNotBlank();
        assertThat(saved.getValue().createdAt()).isEqualTo(NOW);
        assertThat(saved.getValue().expiresAt()).isEqualTo(NOW.plus(Duration.ofDays(7)));
    }

    @Test
    void rejectsAnUnknownEmailWithTheSameErrorAsAWrongPassword() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login("nobody@example.com", "User123!"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(passwordHasher, never()).matches(any(), any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void rejectsAWrongPasswordWithoutIssuingTokens() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(USER));
        when(passwordHasher.matches("wrong", "bcrypt-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login("user@example.com", "wrong"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(accessTokenIssuer, never()).issue(any());
        verify(refreshTokenRepository, never()).save(any());
    }
}
