package com.ecommerce.auth.application;

import com.ecommerce.auth.domain.AccessToken;
import com.ecommerce.auth.domain.Email;
import com.ecommerce.auth.domain.RefreshToken;
import com.ecommerce.auth.domain.User;
import com.ecommerce.auth.domain.exception.InvalidCredentialsException;
import com.ecommerce.auth.domain.port.AccessTokenIssuer;
import com.ecommerce.auth.domain.port.PasswordHasher;
import com.ecommerce.auth.domain.port.RefreshTokenProvider;
import com.ecommerce.auth.domain.port.RefreshTokenRepository;
import com.ecommerce.auth.domain.port.UserRepository;
import com.ecommerce.shared.security.SecurityProperties;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Login: verifica credenciales y abre una sesión (access token + refresh de una familia nueva). */
@Service
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final AccessTokenIssuer accessTokenIssuer;
    private final RefreshTokenProvider refreshTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityProperties securityProperties;
    private final Clock clock;

    public LoginService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            AccessTokenIssuer accessTokenIssuer,
            RefreshTokenProvider refreshTokenProvider,
            RefreshTokenRepository refreshTokenRepository,
            SecurityProperties securityProperties,
            Clock clock) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.accessTokenIssuer = accessTokenIssuer;
        this.refreshTokenProvider = refreshTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.securityProperties = securityProperties;
        this.clock = clock;
    }

    @Transactional
    public AuthResult login(String rawEmail, String rawPassword) {
        User user = userRepository
                .findByEmail(Email.normalize(rawEmail))
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordHasher.matches(rawPassword, user.passwordHash())) {
            throw new InvalidCredentialsException();
        }
        return issueSession(user, UUID.randomUUID().toString());
    }

    private AuthResult issueSession(User user, String familyId) {
        Instant now = clock.instant();
        AccessToken accessToken = accessTokenIssuer.issue(user);
        String rawRefreshToken = refreshTokenProvider.generate();
        RefreshToken refreshToken = RefreshToken.issue(
                refreshTokenProvider.hash(rawRefreshToken),
                familyId,
                user.id(),
                now,
                now.plus(securityProperties.refreshTtl()));
        refreshTokenRepository.save(refreshToken);
        return new AuthResult(user, accessToken, rawRefreshToken);
    }
}
