package com.ecommerce.auth.domain;

/** Access token emitido junto con su vigencia, para construir la respuesta de login/refresh. */
public record AccessToken(String value, long expiresInSeconds) {
}
