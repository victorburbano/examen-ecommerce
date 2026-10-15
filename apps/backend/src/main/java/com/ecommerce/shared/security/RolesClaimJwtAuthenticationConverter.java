package com.ecommerce.shared.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * Mapea el claim {@code roles} del access token a autoridades {@code ROLE_*}.
 * Un token con {@code roles: ["ADMIN"]} produce la autoridad {@code ROLE_ADMIN}.
 */
public final class RolesClaimJwtAuthenticationConverter {

    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    private RolesClaimJwtAuthenticationConverter() {
    }

    public static Converter<Jwt, ? extends org.springframework.security.authentication.AbstractAuthenticationToken>
            create() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName(ROLES_CLAIM);
        authoritiesConverter.setAuthorityPrefix(ROLE_PREFIX);
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }
}
