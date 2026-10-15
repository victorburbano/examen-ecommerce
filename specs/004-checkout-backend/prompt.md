# Prompt 004

```
Implementa la spec specs/004-checkout-backend siguiendo spec.md, plan.md y tasks.md, los skills spec-driven-development, discount-engine, discount-test-matrix, spring-backend-conventions, backend-testing y auth-jwt, y AGENTS.md.
Consulta Context7 para Spring Data JPA (@Version, OptimisticLockException, paginación), Spring Security 7 (@PreAuthorize) y @Transactional antes de usar sus APIs.
Primero un plan corto (≤ 10 líneas) y espera mi OK.
Entrega: quote público y checkout autenticado que recalculan siempre en el servidor con el motor de la spec 003, cupones en BD con seed, stock con @Version y reintento acotado, órdenes persistidas, visibilidad por rol y propiedad, errores ProblemDetail con code, tests (unit, @WebMvcTest, integración H2 con test de concurrencia y rollback) con cobertura ≥ 80 %.
Al terminar: ./mvnw verify en verde, invoca code-auditor, coverage-guard y docs/prompts/security-review.md, dime qué corregiste y regístralo con ia-log. Commit: "feat(checkout): quote, checkout, coupons and stock locking (spec 004)".
```
