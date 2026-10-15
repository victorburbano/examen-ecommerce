# Spec 003 — Motor de descuentos acumulativos (dominio puro)

## Objetivo
Calcular el desglose de descuentos de un carrito de forma secuencial (cascada), con tope del 35 %, aislado de Spring, JPA y HTTP. Es el núcleo evaluable de la prueba.

## Reglas (ver skill `discount-engine`)
1. Categoría: 10 % solo sobre las líneas "Tecnología".
2. Volumen: si el subtotal tras la regla 1 es **> 100.00**, 5 % sobre todo el carrito (ya con la regla 1).
3. Cupón: % del cupón sobre el total tras la regla 2.
4. Tope: descuento total ≤ `round(35 % × subtotal original)`; si se supera se trunca y `capApplied = true`.
Redondeo `HALF_UP`, scale 2, por paso (DEC-007). Parámetros en `DiscountProperties` (DEC-001).

## Modelo (paquete `checkout.domain.discount`, sin dependencias de framework)
- Entrada: `Cart` (`List<CartLine(productId, name, category, unitPrice, quantity)>`), `Optional<AppliedCoupon(code, percent)>`.
- Salida: `DiscountBreakdown(originalSubtotal, steps[], totalDiscount, effectiveDiscountPercent, total, capApplied)`; cada `DiscountStep(type, label, percent, amount)` con `type ∈ CATEGORY, VOLUME, COUPON, CAP_ADJUSTMENT`.
- `DiscountRule` (Strategy) · `DiscountPipeline` · `DiscountPipelineFactory` · `DiscountCapPolicy` · `DiscountProperties`.
- Si se aplica el tope, se añade un paso `CAP_ADJUSTMENT` (monto negativo del exceso devuelto) para que la suma de pasos = descuento total mostrado.

## Criterios de aceptación (valores esperados a mano)
| Caso | Carrito | Cupón | Resultado |
|---|---|---|---|
| Sin descuentos | Hogar 50.00 | — | desc. 0.00 · total 50.00 |
| Solo categoría | Tecnología 40.00 | — | cat 4.00 · total 36.00 |
| Volumen justo | Hogar 100.00 | — | **sin** volumen · total 100.00 |
| Volumen apenas | Hogar 100.01 | — | vol 5.00 (5 % de 100.01 = 5.0005 → 5.00) · total 95.01 |
| Cascada completa | Tecnología 80 + Hogar 50 | 15 % | cat 8.00 · vol 6.10 · cupón 17.39 · desc. 31.49 (24.22 %) · total 98.51 |
| Máximo teórico | Tecnología 200.00 | 15 % | cat 20.00 → 180; vol 9.00 → 171; cupón 25.65 → 145.35; desc. 54.65 (27.33 %), tope no aplica |
| Tope exacto | Hogar 100.00 (sin volumen) | 35 % | desc. 35.00 (35.00 % exacto) · no se trunca · total 65.00 · `capApplied = false` |
| Tope superado | Tecnología 200.00 | 40 % | sin tope sería 20+9+68.40 = 97.40 (48.70 %) → truncado a 70.00 · total 130.00 · `capApplied = true` |

Además: la suma de los pasos = `totalDiscount`; `total = original − totalDiscount`; la cascada ≠ suma de porcentajes; el orden importa (probar con reglas permutadas en un pipeline de test).

## Casos de borde
Carrito vacío → `EmptyCartException`; cantidad ≤ 0, precio negativo, categoría nula, línea nula → `InvalidCartException`; cupón con % ≤ 0 o > 100 → `InvalidCouponException`; subtotal con muchos decimales y HALF_UP en `.005`; propiedades inválidas (tope > 100) fallan al construir.

## Fuera de alcance
Cupones en BD, quote/checkout HTTP, stock y órdenes (spec 004).

## Criterios técnicos
- `domain/discount` no importa `org.springframework`, `jakarta.persistence` ni DTOs (verificado con un test de arquitectura simple basado en `import` o con ArchUnit si se aprueba).
- Cobertura del paquete ≥ 95 % en ramas (es el núcleo).
