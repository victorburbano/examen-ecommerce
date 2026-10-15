package com.ecommerce.checkout.domain.discount;

import java.util.Optional;

/**
 * Strategy: una regla de descuento. Recibe el estado corriente y devuelve su línea del desglose
 * si aplica (o {@code empty} si no). El motor no conoce reglas concretas: solo esta interfaz.
 */
@FunctionalInterface
public interface DiscountRule {

    Optional<DiscountStep> apply(DiscountContext context);
}
