package com.ecommerce.auth.infrastructure;

import com.ecommerce.auth.domain.AccessToken;
import com.ecommerce.auth.domain.User;
import com.ecommerce.auth.domain.port.AccessTokenIssuer;
import com.ecommerce.shared.security.SecurityProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/**
 * Emisor del access token: JWT firmado con HS256 (Nimbus), claims {@code sub} = userId y
 * {@code roles} = lista de roles del usuario.
 */
@Component
public class JwtIssuer implements AccessTokenIssuer {

    private static final String ROLES_CLAIM = "roles";

    private final JwtEncoder jwtEncoder;
    private final SecurityProperties securityProperties;
    private final Clock clock;

    public JwtIssuer(JwtEncoder jwtEncoder, SecurityProperties securityProperties, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.securityProperties = securityProperties;
        this.clock = clock;
    }

    @Override
    public AccessToken issue(User user) {
        Instant issuedAt = clock.instant();
        Duration ttl = securityProperties.accessTtl();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(securityProperties.issuer())
                .subject(String.valueOf(user.id()))
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(ttl))
                .claim(ROLES_CLAIM, List.of(user.role().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AccessToken(token, ttl.toSeconds());
    }
}
