# Plan 005

Skills: `frontend-angular-conventions`, `frontend-testing-jest`, `angular-developer`, `auth-jwt`. Context7: Angular 22 (signals, `resource`/`httpResource`, Signal Forms, router `queryParams`).

## Frontend
- `features/checkout`: `CheckoutStore` (signals: `couponCode`, `quote`, `status`, `error`; `capApplied` como `computed`; re-cotiza con un `effect`/`resource` cuyo parámetro es el carrito + cupón; la respuesta obsoleta se descarta), `CheckoutApiService` (`quote`, `placeOrder`), modelos `Breakdown`, `DiscountLine`, `Order` (`readonly`), `CheckoutPanel`, `BreakdownTable`, `CouponForm` (Signal Forms), `CapAlert` (banner), `OrderConfirmation`.
- `features/orders`: `OrdersPage`, `OrdersApiService` (paginación), ruta `/orders` con `authGuard`; `ADMIN` ve "Todas las órdenes" (`roleGuard` no es necesario: el backend decide el alcance).
- Integración con `CartStore` (spec 002) y `AuthStore` (spec 001); tras comprar: `CartStore.clear()` y recarga del catálogo.
- Error mapper único de `ProblemDetail.code` → mensaje de UI (`COUPON_NOT_FOUND`, `COUPON_EXPIRED`, `INSUFFICIENT_STOCK`, `EMPTY_CART`).
- Tests Jest con `HttpTestingController`; fixtures con los casos de la spec 003/004.
