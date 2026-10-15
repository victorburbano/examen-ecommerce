package com.ecommerce.auth.domain.exception;

/**
 * Credenciales inválidas. El mensaje es genérico a propósito: no se distingue entre
 * email inexistente y contraseña incorrecta (evita enumerar usuarios).
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
