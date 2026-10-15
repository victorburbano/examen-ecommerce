# Tasks 002

## Backend
- [ ] Migraciones `V3__catalog_schema` y `V4__seed_products`
- [ ] Dominio `Product` + excepciones + puerto; adaptador JPA con `@Version`
- [ ] `ProductQueryService`, `ProductController`, DTO con precio string, mapper; rutas públicas
- [ ] Tests unit + `@WebMvcTest` + integración; `./mvnw verify` ≥ 80 %

## Frontend
- [ ] Modelo `Product`, `ProductsApiService`, `CatalogPage`, `ProductCard`
- [ ] `CartStore` (céntimos enteros, tope de stock) y `CartPanel`
- [ ] Layout responsivo y estados loading/error/empty
- [ ] Tests Jest; lint, `test:coverage`, build en verde

## Cierre
- [ ] `code-auditor`, `coverage-guard`; `docs/decisiones.md`, `ia-log`, `MEMORY.md`
- [ ] Commit `feat(catalog): product catalog and reactive cart (spec 002)`
