# Tasks 004

- [ ] Migraciones `V5__checkout_schema`, `V6__seed_coupons`
- [ ] Dominio: `Coupon` (vigencia inclusiva), `Order`/`OrderItem`/`OrderDiscount`, excepciones, puertos
- [ ] Adaptadores JPA, `Clock` bean
- [ ] `QuoteService` (público, sin stock) y `CheckoutService` (transacción única + reintento acotado)
- [ ] Controllers `checkout` y `orders`, DTOs, mapper, `@PreAuthorize` y propiedad
- [ ] `GlobalExceptionHandler`: 400/404/409/422 con `code`
- [ ] Tests: cupón (inexistente, vencido, límites de fecha, normalización), stock (exacto, 0, insuficiente), rollback multi-línea, concurrencia última unidad, roles/propiedad, tope con `MEGA40`
- [ ] `./mvnw verify` ≥ 80 %
- [ ] `security-review`, `code-auditor`, `coverage-guard`; `docs/decisiones.md`, `docs/security.md`, `ia-log`, `MEMORY.md`
- [ ] Commit `feat(checkout): quote, checkout, coupons and stock locking (spec 004)`
