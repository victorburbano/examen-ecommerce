---
name: spring-backend-conventions
description: Convenciones del backend Spring Boot 4 / Java 25 de examen-ecommerce (capas, DTOs, excepciones, SOLID, JPA). Úsalo al escribir o revisar cualquier código Java.
---

# spring-backend-conventions

## Estructura
`com.ecommerce.<module>.{api,application,domain,infrastructure}`. Dependencias: `api → application → domain ← infrastructure`. `domain` no importa Spring Web, JPA ni DTOs (los puertos son interfaces en `domain`).

## Reglas
- **Controller**: solo mapea HTTP ↔ DTO, valida (`@Valid`) y delega. Cero lógica de negocio.
- **DTOs**: `record` `*Request` / `*Response`; mapeo explícito en `*Mapper`. Las entidades JPA nunca salen del módulo.
- **Service** (`application`): un caso de uso con intención clara; `@Transactional` aquí. Constructor injection, campos `final`.
- **Puertos/adaptadores**: interfaz en `domain` (`CouponRepository`, `ProductRepository`, `OrderRepository`); implementación JPA en `infrastructure` (DIP).
- **OCP**: variación por Strategy (`DiscountRule`), nunca `switch` por tipo repartido en el código.
- **Excepciones**: de dominio, sin HTTP (`InsufficientStockException`, `CouponNotFoundException`, `CouponExpiredException`, `EmptyCartException`, `InvalidCartException`…); un único `@RestControllerAdvice` las traduce a `ProblemDetail` (`type`, `title`, `status`, `detail`, `errors[]`). Nunca filtrar stack traces.
- **JPA**: `LAZY` por defecto, sin N+1, `BigDecimal` con `precision/scale`, `@Version` en `Product` (stock). `ddl-auto=validate`; el esquema lo crea Flyway.
- **Jackson 3** en Boot 4 (`tools.jackson.*`): verifica con Context7 antes de importar clases de Jackson.
- **Time**: `Clock` inyectable; prohibido `LocalDate.now()` en lógica.
- Migraciones `V<n>__desc.sql`; seed de datos en migraciones propias (`V2__seed_...`).
- Paginación en listados que puedan crecer (órdenes del ADMIN).

## Checklist
- [ ] ¿Una razón de cambio por clase? ¿Controller sin lógica ni entidades?
- [ ] ¿Excepciones de dominio + handler global? ¿Sin `catch (Exception)`?
- [ ] ¿Sin N+1? ¿`Clock` inyectado?
- [ ] ¿Tests unitarios + `@WebMvcTest` + integración?

## Anti-patrones
Entidad como DTO, lógica en controller, `@Autowired` en campos, `double` para dinero, `ddl-auto=update`, constantes mágicas de negocio.
