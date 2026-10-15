# Tasks 003

- [ ] Matriz de casos con valores esperados calculados a mano (`discount-test-matrix`)
- [ ] Value objects: `CartLine`, `Cart`, `AppliedCoupon`, `DiscountStep`, `DiscountBreakdown`, `DiscountProperties`, `Money`
- [ ] `DiscountRule` + `CategoryDiscountRule`, `VolumeDiscountRule`, `CouponDiscountRule`
- [ ] `DiscountCapPolicy` y `DiscountPipeline` (cascada + paso `CAP_ADJUSTMENT`)
- [ ] `DiscountPipelineFactory`
- [ ] `DiscountConfig` (`app.discounts.*`) y defaults en `application.yml`
- [ ] Tests: tabla parametrizada, reglas, validaciones, orden, tope exacto/superado, test de arquitectura
- [ ] `./mvnw verify` ≥ 80 % global y ≥ 95 % de ramas en `domain/discount`
- [ ] `code-auditor`, `coverage-guard`; `docs/decisiones.md`, `docs/arquitectura.md` (patrones reales), `ia-log`, `MEMORY.md`
- [ ] Commit `feat(checkout): cumulative discount engine with 35% cap (spec 003)`
