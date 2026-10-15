# Plan 004

Skills: `discount-engine`, `discount-test-matrix`, `spring-backend-conventions`, `backend-testing`, `auth-jwt`. Context7: Spring Data JPA (`@Version`, `OptimisticLockException`, paginación), Spring Security 7 (`@PreAuthorize`, `Authentication`), Flyway, `@Transactional`.

## Backend (`com.ecommerce.checkout`)
- `domain`: `Coupon` (code, percent, validFrom, validTo; `assertUsableOn(LocalDate)`), `Order` (+ `OrderItem`, `OrderDiscount`), excepciones `CouponNotFoundException`, `CouponExpiredException`; puertos `CouponRepository`, `OrderRepository`. Reutiliza `ProductRepository` de `catalog` (puerto) y el motor de la spec 003.
- `application`: `QuoteService` (carga productos y cupón, arma `Cart`, usa `DiscountPipelineFactory`), `CheckoutService` (`@Transactional`: recalcula, descuenta stock, persiste, reintento acotado ante `OptimisticLockException` en un componente separado `StockReservation`), `OrderQueryService` (por rol/propiedad). Un caso de uso por clase.
- `infrastructure`: entidades JPA de orden/cupón, repos y adaptadores, `Clock` bean.
- `api`: `CheckoutController`, `OrderController`, DTOs `record` (`CheckoutRequest`, `BreakdownResponse`, `OrderResponse`), `CheckoutMapper`; `@PreAuthorize` en `orders`.
- `shared/error`: añade mapeos 409/422/404/400 con `code` en `GlobalExceptionHandler`.
- Migraciones `V5__checkout_schema.sql`, `V6__seed_coupons.sql`.
- Tests: unit de servicios (Mockito, `Clock` fijo), `@WebMvcTest` (validación, códigos de error, 401/403), integración H2 (flujo completo, rollback, **concurrencia** con `ExecutorService` + `CountDownLatch`, visibilidad por rol).
