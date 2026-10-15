package com.ecommerce.auth.domain.exception;

/** El email ya está registrado (se traduce a 409 EMAIL_TAKEN). */
public class EmailAlreadyTakenException extends RuntimeException {

    public EmailAlreadyTakenException(String email) {
        super("Email already registered: " + email);
    }
}
