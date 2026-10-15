package com.ecommerce.auth.domain.port;

/** Puerto de hashing de contraseñas (BCrypt en infraestructura). */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
