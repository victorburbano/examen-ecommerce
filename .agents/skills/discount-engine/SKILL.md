---
name: discount-engine
description: Reglas de negocio del motor de descuentos acumulativos - orden de precedencia, fórmulas, redondeo, tope del 35 % y cupones. Úsalo al tocar cualquier cálculo de descuento, cupón u orden.
---

# discount-engine

Fuente de las reglas: `docs/requirements.md`. Este skill dicta **cómo** se implementan.

## Reglas (orden fijo)
1. **Categoría**: si hay productos de la categoría configurada ("Tecnología"), 10 % solo sobre el subtotal de esos productos.
2. **Volumen**: si el subtotal tras la regla 1 es **estrictamente mayor** a 100 (100.00 no califica), 5 % sobre todo el carrito (ya con regla 1).
3. **Cupón**: si hay cupón válido, su % sobre el total tras la regla 2 (cascada, no suma).
4. **Tope**: descuento total ≤ `round(35 % × subtotal original)`. Si se supera, se trunca y `capApplied = true`.

Ejemplo: Tecnología 80 + Hogar 50 = 130 → cat 8.00 (122.00) → vol 6.10 (115.90) → cupón 15 % 17.39 (98.51). Total desc. 31.49 (24.22 %).

## Diseño (no negociable)
- Paquete `checkout/domain/discount`, **sin Spring/JPA/HTTP**. Entrada: `Cart` (líneas con precio, cantidad, categoría) + `Optional<AppliedCoupon>`; salida: `DiscountBreakdown` inmutable.
- **Strategy**: interfaz `DiscountRule { DiscountStep apply(DiscountContext) }`; una clase por regla. Nada de `if/else` por tipo.
- **Pipeline**: `DiscountPipeline` ejecuta las reglas en orden; cada una recibe el total corriente. **Factory**: `DiscountPipelineFactory` lo arma por request (la regla de cupón solo entra si hay cupón) con `DiscountProperties`.
- `DiscountCapPolicy` va al final y es la única que conoce el tope. Todo número (10, 5, 100, 35) viene de `DiscountProperties`, nunca hardcodeado.
- Dinero con `BigDecimal` scale 2, `RoundingMode.HALF_UP`, redondeo **por paso** (el desglose suma exacto). Nunca `double`.
- Cuando se aplica el tope, el desglose incluye la línea "ajuste por tope" para que la suma de líneas = descuento total.
- Porcentaje efectivo = descuento total / subtotal original (scale 2).

## Cupones
Tabla `coupons(code, percent, valid_from, valid_to)`; el código se normaliza a mayúsculas y sin espacios. Errores de dominio: `CouponNotFoundException`, `CouponExpiredException` (vigencia con `Clock` inyectado, inclusive en ambos extremos). Cupones seed: `WELCOME2026` 15 % hasta 2026-12-31, `EXPIRED2025` vencido, `MEGA40` 40 % (activa el tope).

## Checkout
`quote` no persiste ni toca stock. `checkout` **recalcula siempre** en el servidor (nunca confía en totales del cliente), valida stock, decrementa con `@Version` y reintento acotado y persiste la orden con el desglose en una sola transacción.

## Anti-patrones
Sumar porcentajes, calcular en el controller o en el front, redondear solo al final, constantes mágicas, confiar en el total enviado por el cliente.
