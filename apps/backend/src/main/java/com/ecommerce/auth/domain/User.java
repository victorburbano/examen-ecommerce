package com.ecommerce.auth.domain;

import java.util.Objects;

/**
 * Usuario de autenticación. Dominio puro: sin anotaciones de persistencia.
 * El email llega ya normalizado (ver {@link Email#normalize(String)}).
 */
public record User(Long id, String email, String passwordHash, Role role) {

    public User {
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        Objects.requireNonNull(role, "role must not be null");
    }

    /** Usuario nuevo: sin id (lo asigna la persistencia) y con rol USER. */
    public static User newUser(String email, String passwordHash) {
        return new User(null, email, passwordHash, Role.USER);
    }

    public User withId(Long assignedId) {
        return new User(assignedId, email, passwordHash, role);
    }
}
