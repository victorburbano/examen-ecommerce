# Tasks 003

- [x] Matriz de casos con valores esperados calculados a mano (`discount-test-matrix`)
- [x] Value objects: `CartLine`, `Cart`, `AppliedCoupon`, `DiscountStep`, `DiscountBreakdown`, `DiscountProperties`, `Money`
- [x] `DiscountRule` + `CategoryDiscountRule`, `VolumeDiscountRule`, `CouponDiscountRule`
- [x] `DiscountCapPolicy` y `DiscountPipeline` (cascada + paso `CAP_ADJUSTMENT`)
- [x] `DiscountPipelineFactory`
- [x] `DiscountConfig` (`app.discounts.*`) y defaults en `application.yml`
- [x] Tests: tabla parametrizada, reglas, validaciones, orden, tope exacto/superado, test de arquitectura
- [x] `./mvnw verify` ≥ 80 % global y ≥ 95 % de ramas en `domain/discount`
- [x] `code-auditor`, `coverage-guard`; `docs/decisiones.md`, `docs/arquitectura.md` (patrones reales), `ia-log`, `MEMORY.md`
- [x] Commit `feat(checkout): cumulative discount engine with 35% cap (spec 003)`
