package com.ecommerce.checkout.domain.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Único lugar del motor con reglas de redondeo: escala 2 y {@link RoundingMode#HALF_UP} (DEC-007).
 * Todo importe del desglose pasa por aquí, de modo que la suma de pasos cuadra al céntimo.
 */
public final class Money {

    public static final int SCALE = 2;
    public static final BigDecimal HUNDRED = new BigDecimal("100");

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(SCALE);

    private Money() {
    }

    public static BigDecimal zero() {
        return ZERO;
    }

    /** Redondea a 2 decimales con HALF_UP. */
    public static BigDecimal round(BigDecimal amount) {
        return amount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /** Porcentaje de un importe, redondeado a 2 decimales (HALF_UP). */
    public static BigDecimal percentOf(BigDecimal amount, BigDecimal percent) {
        return amount.multiply(percent).divide(HUNDRED, SCALE, RoundingMode.HALF_UP);
    }

    /** Relación {@code part / whole} expresada en porcentaje, redondeada a 2 decimales. */
    public static BigDecimal ratioAsPercent(BigDecimal part, BigDecimal whole) {
        if (whole.signum() == 0) {
            return ZERO;
        }
        return part.multiply(HUNDRED).divide(whole, SCALE, RoundingMode.HALF_UP);
    }
}
