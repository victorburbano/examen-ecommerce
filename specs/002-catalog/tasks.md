# Tasks 002

## Backend
- [x] Migraciones `V3__catalog_schema` y `V4__seed_products`
- [x] Dominio `Product` + excepciones + puerto; adaptador JPA con `@Version`
- [x] `ProductQueryService`, `ProductController`, DTO con precio string, mapper; rutas públicas
- [x] Tests unit + `@WebMvcTest` + integración; `./mvnw verify` ≥ 80 %

## Frontend
- [x] Modelo `Product`, `ProductsApiService`, `CatalogPage`, `ProductCard`
- [x] `CartStore` (céntimos enteros, tope de stock) y `CartPanel`
- [x] Layout responsivo y estados loading/error/empty
- [x] Tests Jest; lint, `test:coverage`, build en verde

## Cierre
- [x] `code-auditor`, `coverage-guard`; `docs/decisiones.md`, `ia-log`, `MEMORY.md`
- [x] Commit `feat(catalog): product catalog and reactive cart (spec 002)`
