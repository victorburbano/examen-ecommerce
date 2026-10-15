package com.ecommerce.auth.api;

import com.ecommerce.auth.domain.AccessToken;

/** Cuerpo de login y refresh. El refresh token viaja aparte, en una cookie HttpOnly. */
public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {

    public static TokenResponse bearer(AccessToken accessToken) {
        return new TokenResponse(accessToken.value(), "Bearer", accessToken.expiresInSeconds());
    }
}
