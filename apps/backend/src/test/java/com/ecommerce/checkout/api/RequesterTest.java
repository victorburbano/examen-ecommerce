package com.ecommerce.checkout.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

/** Extracción del peticionario desde el JWT: {@code sub} y claim {@code roles}. */
class RequesterTest {

    private static Jwt jwt(String subject, List<String> roles) {
        Jwt.Builder builder = Jwt.withTokenValue("token").header("alg", "none").subject(subject);
        if (roles != null) {
            builder.claim("roles", roles);
        }
        return builder.build();
    }

    @Test
    void readsTheUserIdFromTheSubject() {
        assertThat(Requester.from(jwt("7", List.of("USER"))).userId()).isEqualTo(7L);
    }

    @Test
    void flagsAdmins() {
        assertThat(Requester.from(jwt("1", List.of("ADMIN"))).admin()).isTrue();
        assertThat(Requester.from(jwt("1", List.of("USER", "ADMIN"))).admin()).isTrue();
    }

    @Test
    void aPlainUserIsNotAnAdminAndAMissingRolesClaimDoesNotFail() {
        assertThat(Requester.from(jwt("7", List.of("USER"))).admin()).isFalse();
        assertThat(Requester.from(jwt("7", null)).admin()).isFalse();
    }
}
