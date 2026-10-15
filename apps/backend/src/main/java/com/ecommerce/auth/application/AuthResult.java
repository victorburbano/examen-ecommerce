package com.ecommerce.auth.application;

import com.ecommerce.auth.domain.AccessToken;
import com.ecommerce.auth.domain.User;

/**
 * Resultado de una autenticación correcta: el access token para el cuerpo de la respuesta
 * y el refresh token en claro (solo viaja en la cookie; en BD se guarda su hash).
 */
public record AuthResult(User user, AccessToken accessToken, String refreshToken) {
}
