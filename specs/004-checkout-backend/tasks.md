# Tasks 004

- [x] Migraciones `V5__checkout_schema`, `V6__seed_coupons`
- [x] Dominio: `Coupon` (vigencia inclusiva), `Order`/`OrderItem`/`OrderDiscount`, excepciones, puertos
- [x] Adaptadores JPA, `Clock` bean
- [x] `QuoteService` (público, sin stock) y `CheckoutService` (transacción única + reintento acotado)
- [x] Controllers `checkout` y `orders`, DTOs, mapper, `@PreAuthorize` y propiedad
- [x] `GlobalExceptionHandler`: 400/404/409/422 con `code`
- [x] Tests: cupón (inexistente, vencido, límites de fecha, normalización), stock (exacto, 0, insuficiente), rollback multi-línea, concurrencia última unidad, roles/propiedad, tope con `MEGA40`
- [x] `./mvnw verify` ≥ 80 % (290 tests; LINE 99.8 % / BRANCH 99.4 %)
- [x] `security-review`, `code-auditor`, `coverage-guard`; `docs/decisiones.md`, `docs/security.md`, `ia-log`, `MEMORY.md`
- [x] Commit `feat(checkout): quote, checkout, coupons and stock locking (spec 004)`
