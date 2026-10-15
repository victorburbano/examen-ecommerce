# examen-ecommerce — Core E-Commerce con descuentos acumulativos

Prueba técnica full stack: carrito y checkout con motor de **descuentos acumulativos** (categoría → volumen → cupón, con tope del 35 %), backend Java + frontend Angular, construida con **Spec-Driven Development** asistido por IA.

> **Estado**: spec 000 (foundation) completa. Las specs 001–006 se implementan una a una; este README se cierra en la spec 006. Seguimiento en [MEMORY.md](MEMORY.md).

## Stack (verificado 2026-10-15)
Java 25 · Spring Boot 4.1.1 · Spring Data JPA + Flyway + H2 en memoria · Spring Security (JWT, Nimbus) · Angular 22 (standalone, signals, zoneless) · TypeScript ~6.0 · Jest 30 · JaCoCo 0.8.15.

## Requisitos
JDK 25, Node `^22.22.3 || ^24.15.0 || ^26`, npm. Maven no hace falta (wrapper incluido).

## Inicio rápido
```bash
# Backend (terminal 1)
cd apps/backend
./mvnw spring-boot:run            # http://localhost:8080

# Frontend (terminal 2)
cd apps/frontend
npm ci
npm start                         # http://localhost:4200
```

## Pruebas y cobertura (umbral 80 %)
```bash
cd apps/backend  && ./mvnw verify               # tests + JaCoCo (falla si LINE o BRANCH < 80 %)
                                                # reporte: apps/backend/target/site/jacoco/index.html
cd apps/frontend && npm run test:coverage       # Jest; reporte en apps/frontend/coverage/
cd apps/frontend && npm run lint && npm run build
```

## Variables de entorno
Ver [.env.example](.env.example). `JWT_SECRET` (≥ 32 bytes) es obligatorio desde la spec 001; nunca se versiona `.env`.

## Usuarios y cupones de prueba
Usuarios seed (contraseñas **de prueba**, solo para local; `V2__seed_users.sql`):

| Email | Contraseña | Rol |
|---|---|---|
| `admin@example.com` | `Admin123!` | `ADMIN` |
| `user@example.com` | `User123!` | `USER` |

Cupones (se documentan en la spec 004): `WELCOME2026`, `EXPIRED2025`, `MEGA40`.

### Endpoints de auth (spec 001)
`POST /api/auth/register` · `POST /api/auth/login` · `POST /api/auth/refresh` · `POST /api/auth/logout` · `GET /api/auth/me`.
El refresh token viaja en una cookie `HttpOnly` (`Path=/api/auth`); el access token solo vive en memoria en el SPA. El frontend alcanza la API por el proxy de `npm start` (`proxy.conf.json` → `http://localhost:8080`).

## Cómo se construye (SDD)
Cada funcionalidad nace de una spec en [specs/](specs/) (`spec.md` → `plan.md` → `tasks.md` → `prompt.md`) y se entrega en **un commit por spec**.

| # | Spec | HU |
|---|---|---|
| 000 | foundation | — |
| 001 | auth | — |
| 002 | catalog (catálogo y carrito) | HU 1 |
| 003 | discount-engine | núcleo |
| 004 | checkout-backend | HU 3 |
| 005 | checkout-frontend | HU 2, HU 4 |
| 006 | docs-governance | — |

## Documentación
- [Requisitos](docs/requirements.md) · [Decisiones](docs/decisiones.md) · [Arquitectura](docs/arquitectura.md) · [Gobernanza de IA](docs/ia.md) · [Seguridad](docs/security.md)
- Convenciones para agentes de IA: [AGENTS.md](AGENTS.md) · Skills y agentes: [docs/skills-registry.md](docs/skills-registry.md)

## Estructura
```
apps/backend/    # Spring Boot (módulos auth, catalog, checkout, shared)
apps/frontend/   # Angular 22
specs/           # Specs SDD
docs/            # arquitectura.md, ia.md, decisiones.md, requirements.md, security.md, prompts/
.agents/skills/  # Skills de IA (fuente real); .claude/skills = symlinks
```

### Skills en Windows
`.claude/skills/` usa symlinks. Con el Modo desarrollador activo, `scripts/sync-agent-links.ps1` los recrea (en Linux/macOS, `scripts/sync-agent-links.sh`).
