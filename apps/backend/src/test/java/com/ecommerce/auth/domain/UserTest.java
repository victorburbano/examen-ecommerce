package com.ecommerce.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void newUserStartsWithUserRoleAndNoId() {
        User user = User.newUser("user@example.com", "hash");

        assertThat(user.id()).isNull();
        assertThat(user.email()).isEqualTo("user@example.com");
        assertThat(user.passwordHash()).isEqualTo("hash");
        assertThat(user.role()).isEqualTo(Role.USER);
    }

    @Test
    void withIdKeepsTheRestOfTheState() {
        User user = User.newUser("user@example.com", "hash").withId(7L);

        assertThat(user.id()).isEqualTo(7L);
        assertThat(user.email()).isEqualTo("user@example.com");
        assertThat(user.role()).isEqualTo(Role.USER);
    }

    @Test
    void rejectsNullComponents() {
        assertThatThrownBy(() -> new User(1L, null, "hash", Role.USER)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new User(1L, "a@b.com", null, Role.USER)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new User(1L, "a@b.com", "hash", null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void exposesAdminRole() {
        assertThat(new User(1L, "admin@example.com", "hash", Role.ADMIN).role()).isEqualTo(Role.ADMIN);
        assertThat(NOW).isNotNull();
    }
}
