# Plan 003

Skills: `discount-engine`, `discount-test-matrix`, `spring-backend-conventions`, `backend-testing`. Sin Context7 necesario en el dominio (Java puro); sí para `@ConfigurationProperties` en Boot 4.

## Paquete `com.ecommerce.checkout.domain.discount`
- Value objects inmutables (records): `CartLine`, `Cart` (valida en el constructor compacto), `AppliedCoupon`, `DiscountStep`, `DiscountBreakdown`, `DiscountProperties` (categoryName, categoryPercent, volumeThreshold, volumePercent, maxTotalPercent; valida rangos).
- `DiscountContext(cart, originalSubtotal, runningTotal, coupon)` y `DiscountRule { Optional<DiscountStep> apply(DiscountContext) }`.
- Reglas: `CategoryDiscountRule`, `VolumeDiscountRule` (estrictamente `>`), `CouponDiscountRule`.
- `DiscountPipeline(List<DiscountRule>, DiscountCapPolicy)`: itera en orden, actualiza `runningTotal`, aplica el tope y construye `DiscountBreakdown`.
- `DiscountPipelineFactory(DiscountProperties)`: `create(Optional<AppliedCoupon>)` → la regla de cupón solo entra si hay cupón.
- `Money` utilitario (`round`, `percentOf`) con `HALF_UP` scale 2; único lugar con `RoundingMode`.
- Excepciones de dominio: `EmptyCartException`, `InvalidCartException`, `InvalidCouponException`.

## Infraestructura mínima
- `infrastructure/config/DiscountConfig`: `@ConfigurationProperties(prefix = "app.discounts")` + `@Bean DiscountProperties` y `@Bean DiscountPipelineFactory`; defaults en `application.yml` (`category-name: Tecnología`, 10, 100.00, 5, 35).

## Tests
- `DiscountPipelineTest`: `@ParameterizedTest` sobre la tabla del spec (generada con `discount-test-matrix`; esperados a mano).
- Reglas individuales, `Cart` (validaciones), `DiscountProperties` (rangos), `Money` (HALF_UP en .005), pipeline permutado (el orden importa), tope exacto / superado con pipeline agresivo.
- Test de arquitectura: ningún archivo de `domain/discount` importa Spring/JPA.
