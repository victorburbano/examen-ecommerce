package com.ecommerce.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BcryptPasswordHasherTest {

    private static final String PASSWORD = "User123!";

    private final BcryptPasswordHasher hasher = new BcryptPasswordHasher(new BCryptPasswordEncoder());

    @Test
    void hashIsBcryptAndDoesNotLeakThePassword() {
        String hash = hasher.hash(PASSWORD);

        assertThat(hash).startsWith("$2a$10$").doesNotContain(PASSWORD);
    }

    @Test
    void matchesAcceptsTheRightPasswordAndRejectsOthers() {
        String hash = hasher.hash(PASSWORD);

        assertThat(hasher.matches(PASSWORD, hash)).isTrue();
        assertThat(hasher.matches("wrong-password", hash)).isFalse();
    }
}
