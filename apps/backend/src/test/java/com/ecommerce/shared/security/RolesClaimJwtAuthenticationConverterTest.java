package com.ecommerce.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class RolesClaimJwtAuthenticationConverterTest {

    @Test
    void mapsTheRolesClaimToRolePrefixedAuthorities() {
        AbstractAuthenticationToken authentication =
                RolesClaimJwtAuthenticationConverter.create().convert(jwtWithRoles(List.of("USER", "ADMIN")));

        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void yieldsNoRoleAuthoritiesWhenTheClaimIsAbsent() {
        AbstractAuthenticationToken authentication =
                RolesClaimJwtAuthenticationConverter.create().convert(jwtWithRoles(null));

        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .noneMatch(authority -> authority.startsWith("ROLE_"));
    }

    private static Jwt jwtWithRoles(List<String> roles) {
        Instant now = Instant.now();
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("1")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(900));
        if (roles != null) {
            builder.claim("roles", roles);
        }
        return builder.build();
    }
}
