package com.ecommerce.auth.domain;

import java.util.Locale;

/**
 * Normalización de emails: recorta espacios y pasa a minúsculas para que
 * {@code User@Example.com} y {@code user@example.com} sean el mismo usuario.
 */
public final class Email {

    private Email() {
    }

    public static String normalize(String rawEmail) {
        if (rawEmail == null) {
            throw new IllegalArgumentException("email must not be null");
        }
        return rawEmail.trim().toLowerCase(Locale.ROOT);
    }
}
