package com.ecommerce.checkout.api;

import java.util.List;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Identidad del peticionario extraída del access token ({@code sub} = userId, claim
 * {@code roles}). Es solo mapeo: no hay reglas de negocio aquí.
 */
public record Requester(Long userId, boolean admin) {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String ROLES_CLAIM = "roles";

    public static Requester from(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM);
        boolean admin = roles != null && roles.contains(ADMIN_ROLE);
        return new Requester(Long.valueOf(jwt.getSubject()), admin);
    }
}
