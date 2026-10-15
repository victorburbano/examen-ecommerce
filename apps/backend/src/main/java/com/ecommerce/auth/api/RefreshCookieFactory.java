package com.ecommerce.auth.api;

import com.ecommerce.shared.security.SecurityProperties;
import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Cookie del refresh token: {@code HttpOnly}, {@code SameSite=Strict} y acotada al path
 * {@code /api/auth} para que no la envíe ninguna otra petición. {@code Secure} lo fija
 * {@code COOKIE_SECURE} (false solo en desarrollo por HTTP).
 */
@Component
public class RefreshCookieFactory {

    public static final String COOKIE_NAME = "refresh_token";
    private static final String COOKIE_PATH = "/api/auth";

    private final SecurityProperties properties;

    public RefreshCookieFactory(SecurityProperties properties) {
        this.properties = properties;
    }

    public ResponseCookie create(String rawRefreshToken) {
        return baseBuilder(rawRefreshToken).maxAge(properties.refreshTtl()).build();
    }

    /** Cookie de borrado: mismo path y atributos, valor vacío y {@code Max-Age=0}. */
    public ResponseCookie clear() {
        return baseBuilder("").maxAge(Duration.ZERO).build();
    }

    private ResponseCookie.ResponseCookieBuilder baseBuilder(String value) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(properties.cookieSecure())
                .sameSite("Strict")
                .path(COOKIE_PATH);
    }
}
