package com.ecommerce.auth.domain.port;

/** Genera refresh tokens opacos y calcula su hash para almacenarlos. */
public interface RefreshTokenProvider {

    /** Token aleatorio nuevo (nunca se guarda en claro). */
    String generate();

    /** Hash determinista del token, para persistir y buscar por él. */
    String hash(String rawToken);
}
