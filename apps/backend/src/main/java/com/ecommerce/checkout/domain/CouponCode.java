package com.ecommerce.checkout.domain;

import java.util.Locale;
import java.util.Optional;

/**
 * Normalización del código de cupón: se quitan los espacios de los extremos y se pasa a
 * mayúsculas. Un código nulo o vacío significa "sin cupón" ({@link Optional#empty()}).
 */
public final class CouponCode {

    private CouponCode() {
    }

    public static Optional<String> normalize(String rawCode) {
        if (rawCode == null) {
            return Optional.empty();
        }
        String normalized = rawCode.trim().toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? Optional.empty() : Optional.of(normalized);
    }
}
