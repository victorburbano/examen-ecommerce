# examen-ecommerce — Core E-Commerce con descuentos acumulativos

Prueba técnica full stack: carrito y checkout con motor de **descuentos acumulativos** (categoría → volumen → cupón, con tope del 35 %), backend Java + frontend Angular, construida con **Spec-Driven Development** asistido por IA.

> **Estado**: specs **000–006 completas** (un commit por spec). Método, decisiones y bitácora de IA en [docs/](docs/) y [MEMORY.md](MEMORY.md).

## Stack (verificado 2026-10-15)
Java 25 · Spring Boot 4.1.1 · Spring Data JPA + Flyway + H2 en memoria · Spring Security (JWT con Nimbus) · Angular 22 (standalone, signals, zoneless) · TypeScript ~6.0 · Jest 30 · JaCoCo 0.8.15.

## Requisitos
- **JDK 25** (boot 4.1 exige ≥ 17; aquí 25 LTS).
- **Node** `^22.22.3 || ^24.15.0 || ^26` y npm.
- Maven **no** hace falta: el wrapper (`.mvn/wrapper`) está incluido.

## Inicio rápido
```bash
# Backend (terminal 1)
cd apps/backend
export JWT_SECRET="$(openssl rand -base64 48)"   # obligatorio, ≥ 32 bytes (ver .env.example)
./mvnw spring-boot:run                            # http://localhost:8080

# Frontend (terminal 2)
cd apps/frontend
npm ci
npm start                                         # http://localhost:4200
```
> En **Windows/PowerShell**: `$env:JWT_SECRET = "..."` y `.\mvnw.cmd spring-boot:run`. El frontend llama a la API por el proxy de `npm start` (`proxy.conf.json` → `http://localhost:8080`), así que basta con abrir `http://localhost:4200`.

## Variables de entorno
Copia [.env.example](.env.example) a `.env` (nunca se versiona `.env`). Todas tienen valor por defecto **salvo `JWT_SECRET`**, que es obligatorio:

| Variable | Por defecto | Descripción |
|---|---|---|
| `JWT_SECRET` | — (**obligatorio**) | Secreto HS256, ≥ 32 bytes |
| `JWT_ISSUER` | `examen-ecommerce` | Emisor del JWT |
| `JWT_ACCESS_TTL_MINUTES` | `15` | Vida del access token |
| `JWT_REFRESH_TTL_DAYS` | `7` | Vida del refresh token |
| `CORS_ALLOWED_ORIGIN` | `http://localhost:4200` | Origen permitido del SPA |
| `COOKIE_SECURE` | `false` | `true` solo sirviendo por HTTPS |
| `DISCOUNT_CATEGORY` | `TECHNOLOGY` | Categoría del descuento 1 (nombre del enum) |
| `DISCOUNT_CATEGORY_PERCENT` | `10` | % categoría |
| `DISCOUNT_VOLUME_THRESHOLD` | `100.00` | Umbral del descuento por volumen (estricto) |
| `DISCOUNT_VOLUME_PERCENT` | `5` | % volumen |
| `DISCOUNT_MAX_TOTAL_PERCENT` | `35` | Tope del descuento total |

## Usuarios y cupones de prueba
Usuarios seed (`V2__seed_users.sql`; contraseñas **de prueba**, solo para local):

| Email | Contraseña | Rol |
|---|---|---|
| `admin@example.com` | `Admin123!` | `ADMIN` |
| `user@example.com` | `User123!` | `USER` |

Cupones seed (`V6__seed_coupons.sql`):

| Código | Descuento | Vigencia | Uso en la demo |
|---|---|---|---|
| `WELCOME2026` | 15 % | 2026-01-01 → 2026-12-31 | Desglose con cupón, sin tope |
| `MEGA40` | 40 % | 2026-01-01 → 2026-12-31 | Alcanza el **tope del 35 %** (banner) |
| `EXPIRED2025` | 10 % | 2025 (vencido) | Error de cupón caducado |

Productos seed (`V4__seed_products.sql`): ids 1–8, con uno de stock bajo (id 6, stock 2) y uno sin stock (id 8, stock 0) para probar el 409.

## Pruebas y cobertura (umbral 80 %)
```bash
# Backend: tests + JaCoCo (el build FALLA si LINE o BRANCH < 80 %)
cd apps/backend && ./mvnw verify
#   reporte: apps/backend/target/site/jacoco/index.html

# Frontend: Jest con cobertura (umbral 80 % en jest.config.js)
cd apps/frontend && npm run test:coverage
#   reporte: apps/frontend/coverage/lcov-report/index.html

# Frontend: lint + build
cd apps/frontend && npm run lint && npm run build
```
Cifras actuales (verificadas 2026-10-15):

| Suite | Tests | Cobertura |
|---|---|---|
| Backend | **292** (0 fallos) | LINE **99.79 %** (971/973) · BRANCH **99.44 %** (179/180) |
| Frontend | **143** | **100 %** statements / branches / functions / lines |

## API
| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `POST` | `/api/auth/register` | público | Alta de usuario |
| `POST` | `/api/auth/login` | público | Login; devuelve access token y fija la cookie de refresh |
| `POST` | `/api/auth/refresh` | cookie | Rota el refresh (detección de reutilización) |
| `POST` | `/api/auth/logout` | cookie | Cierra la sesión |
| `GET` | `/api/auth/me` | Bearer | Usuario actual |
| `GET` | `/api/products` · `/api/products/{id}` | público | Catálogo (precio como **string** decimal exacto) |
| `POST` | `/api/checkout/quote` | público | Desglose de descuentos (categoría, volumen, cupón, tope) |
| `POST` | `/api/checkout/orders` | `USER`/`ADMIN` | Compra: valida stock, recalcula, descuenta y persiste |
| `GET` | `/api/orders?page&size` | `USER`/`ADMIN` | Órdenes: `USER` solo las suyas, `ADMIN` todas |
| `GET` | `/api/orders/{id}` | `USER`/`ADMIN` | Detalle (ajena → 404) |
| `GET` | `/actuator/health` | público | Salud |

Errores como `ProblemDetail` (RFC 9457) con `code` estable (`EMPTY_CART`, `INVALID_CART`, `COUPON_NOT_FOUND`, `COUPON_EXPIRED`, `INSUFFICIENT_STOCK`, `PRODUCT_NOT_FOUND`, `VALIDATION_ERROR`, …). El **backend es la única fuente de cálculo**; el front solo pide el desglose.

## Reglas de negocio (en cascada)
1. **Categoría**: 10 % (configurable) sobre las líneas de la categoría configurada.
2. **Volumen**: 5 % sobre el total corriente si supera **estrictamente** 100.00 (100.00 no aplica, 100.01 sí).
3. **Cupón**: % del cupón vigente sobre el total tras la regla 2.
4. **Tope**: el descuento total nunca supera el 35 % del subtotal original; si lo supera, se añade una línea de ajuste negativa y `capApplied = true`.

Detalle y casos de borde en [docs/requirements.md](docs/requirements.md) y [docs/decisiones.md](docs/decisiones.md).

## Cómo se construye (SDD)
Cada funcionalidad nace de una spec en [specs/](specs/) (`spec.md` → `plan.md` → `tasks.md` → `prompt.md`) y se entrega en **un commit por spec**.

| # | Spec | Contenido |
|---|---|---|
| 000 | foundation | Esqueletos back y front |
| 001 | auth | Register/login/refresh/logout, roles `USER`/`ADMIN` |
| 002 | catalog | Catálogo y carrito reactivo (HU 1) |
| 003 | discount-engine | Motor de descuentos de dominio puro |
| 004 | checkout-backend | `quote` + `orders`, cupones en BD, stock con `@Version` (HU 3) |
| 005 | checkout-frontend | Cupón, desglose, banner del tope, compra y órdenes (HU 2 y HU 4) |
| 006 | docs-governance | Arquitectura, gobernanza de IA, README y demo |

## Estructura
```
apps/backend/     # Spring Boot 4.1 (módulos auth, catalog, checkout, shared)
  src/main/java/com/ecommerce/{auth,catalog,checkout,shared}/{api,application,domain,infrastructure}
  src/main/resources/db/migration/   # Flyway V1..V6 (schema + seed)
apps/frontend/    # Angular 22 (standalone, signals)
  src/app/{core,shared,features/{auth,catalog,cart,checkout,orders}}
specs/            # Specs SDD (000..006)
docs/             # arquitectura.md, ia.md, decisiones.md, requirements.md, security.md, demo.md, prompts/
.agents/skills/   # Skills de IA (fuente real); .claude/skills = symlinks
```

## Documentación
- [Arquitectura](docs/arquitectura.md) · [Gobernanza de IA](docs/ia.md) · [Decisiones](docs/decisiones.md) · [Requisitos](docs/requirements.md) · [Seguridad](docs/security.md) · [Guion de la demo](docs/demo.md)
- Convenciones para agentes: [AGENTS.md](AGENTS.md) · Skills y sub-agentes: [docs/skills-registry.md](docs/skills-registry.md) · Estado y continuidad: [MEMORY.md](MEMORY.md)
- CI: [.github/workflows/ci.yml](.github/workflows/ci.yml) (backend `mvnw verify`, frontend lint + test:coverage + build).

### Skills en Windows
`.claude/skills/` usa symlinks. Con el Modo desarrollador activo, `scripts/sync-agent-links.ps1` los recrea (en Linux/macOS, `scripts/sync-agent-links.sh`).
