# Plan 002

Skills: `spring-backend-conventions`, `backend-testing`, `frontend-angular-conventions`, `frontend-testing-jest`, `angular-developer`.

## Backend (`com.ecommerce.catalog`)
- `domain`: `Product` (id, name, category, price `BigDecimal`, stock, `@Version` en la entidad JPA), `InsufficientStockException`, `ProductNotFoundException`, puerto `ProductRepository` (`findAll`, `findById`, `findAllById`).
- `application`: `ProductQueryService` (`list`, `get`).
- `infrastructure`: `ProductEntity` + `ProductJpaRepository` + adaptador `JpaProductRepository`; mapeo entidad ↔ dominio explícito.
- `api`: `ProductController`, `ProductResponse` (record con precio como string vía `@JsonFormat(shape = STRING)`), `ProductMapper`. Endpoints públicos en `SecurityConfig`.
- Migraciones: `V3__catalog_schema.sql` (`products`: `id`, `name`, `category`, `price DECIMAL(10,2)`, `stock INT CHECK >= 0`, `version BIGINT`), `V4__seed_products.sql`.
- Tests: unit de `Product.decreaseStock`; `@WebMvcTest` del controller; integración H2 (seed completo, `@Version`).

## Frontend
- `features/catalog`: `CatalogPage`, `ProductCard`, `ProductsApiService`, modelo `Product` (`readonly`).
- `features/cart`: `CartStore` (signals privados; `items`, `itemCount`, `subtotal` como `computed`; subtotal en **céntimos enteros** para evitar errores de coma flotante), `CartPanel` (lista, +/−, vaciar).
- Layout responsivo con CSS Grid; estados `loading`/`error`/`empty` con `resource`/`httpResource`.
- Tests Jest: `CartStore` (agregar, tope de stock, quitar, vaciar, subtotal exacto), `CatalogPage` (estados), `ProductCard` (deshabilitado sin stock).
