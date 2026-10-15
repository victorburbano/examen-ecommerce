package com.ecommerce.auth.domain.exception;

/** Refresh token ausente, inexistente, expirado, revocado o reutilizado (se traduce a 401). */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Invalid or expired refresh token");
    }
}
