# AGENTS.md — examen-ecommerce

Fuente única de verdad para cualquier agente de IA (Codex, Cursor, Copilot, Gemini CLI, Claude Code vía `CLAUDE.md`).

> **Memoria del proyecto**: lee [MEMORY.md](MEMORY.md) **al empezar** cada sesión y **actualízalo al terminar**.
> **Método**: Spec-Driven Development. Nada se codifica sin su spec en [specs/](specs/). Lee el skill `spec-driven-development`.

## Objetivo
Prueba técnica full stack: **checkout de e-commerce con descuentos acumulativos**. Backend calcula y persiste; frontend gestiona el carrito y muestra el desglose. Se defiende en 20 min (7 demo · 7 arquitectura · 6 auditoría de IA).
Se evalúa: arquitectura y patrones (≥ 2 implementados), tipado estricto de extremo a extremo (sin `any`), cobertura ≥ 80 % en front y back, casos de borde y gobernanza de IA (`docs/ia.md`).
Enunciado resumido y reglas exactas en [docs/requirements.md](docs/requirements.md).

## Stack (verificado 2026-10-15)
| Tecnología | Versión | Fuente |
|---|---|---|
| Java | 25 LTS (Boot 4.1 soporta Java 17 a 26; `java -version` local confirma 25 LTS) | docs.spring.io/spring-boot/4.1/system-requirements |
| Spring Boot | 4.1.1 (Framework ≥ 7.0.9 vía BOM). 4.2.0-M2 es milestone: no usar | docs.spring.io, Maven Central |
| Maven | wrapper 3.9.11 (Boot exige ≥ 3.6.3) | `.mvn/wrapper` |
| Base de datos | H2 en memoria + Flyway (versión gestionada por el BOM de Boot) | [DEC-003](docs/decisiones.md) |
| JaCoCo | 0.8.15 | Maven Central |
| Angular | 22.2.x (`latest` npm = 22.2.3) | npm |
| TypeScript | ~6.0.3 (Angular 22 exige `>=6.0 <6.1`; **no usar 7.x**) | angular.dev/reference/versions |
| Node.js | ^22.22.3 \|\| ^24.15.0 \|\| ^26 | angular.dev/reference/versions |
| Jest / jest-preset-angular | 30.5.2 / 17.0.3 | npm (el equipo ya usa Jest; Vitest es el default de Angular) |

Re-verifica con el skill `verify-versions` si pasó tiempo.

## Arquitectura
Monolito modular con capas ligeras tipo hexagonal. Módulos: `auth`, `catalog`, `checkout`, `shared`.
Capas por módulo: `api` (controllers + DTOs) → `application` (casos de uso) → `domain` (entidades, reglas, **puertos**) ← `infrastructure` (JPA, adaptadores).
- **Motor de descuentos = dominio puro** (`checkout/domain/discount`): sin Spring, sin JPA, sin HTTP. Patrones: **Strategy** (`DiscountRule`), **Pipeline/Chain** ordenado (`DiscountPipeline`), **Factory** (`DiscountPipelineFactory`), **Port/Adapter** (repositorios).
- Reglas (en orden de precedencia): `CategoryDiscountRule` → `VolumeDiscountRule` → `CouponDiscountRule` → `DiscountCapPolicy`. Porcentajes, umbral y tope en `application.yml` (`app.discounts.*`); el % del cupón viene de la BD.
- Dinero: `BigDecimal`, 2 decimales, `HALF_UP`, redondeo por paso (el desglose suma exacto). Tope = `round(0.35 × subtotal original)`; si se supera, el desglose añade la línea "ajuste por tope" y `capApplied = true`.
- El **backend es la única fuente de cálculo**. El front solo suma el subtotal local y pide el desglose a `POST /api/checkout/quote`.
- Stock: `@Version` (optimistic locking) + reintento acotado; la regla "no bajar de 0" vive en la entidad `Product`.
- Auth: access JWT 15 min (Nimbus vía `oauth2-resource-server`, **sin JJWT**), refresh opaco rotado y guardado como hash con detección de reutilización, cookie `HttpOnly`. Roles `USER` (catálogo, cotizar, comprar, sus órdenes) y `ADMIN` (todas las órdenes). Access token solo en memoria en el SPA.
- Frontend: standalone, signals (`signal`, `computed`, `linkedSignal`), zoneless, `OnPush`, `strict`, lazy routes, interceptor de auth con refresh único, CSS propio responsivo (sin librería de UI).
- Errores: `@RestControllerAdvice` global con `ProblemDetail` (RFC 9457).

Detalle en [docs/arquitectura.md](docs/arquitectura.md). Decisiones en [docs/decisiones.md](docs/decisiones.md).

## Estructura
```
specs/NNN-nombre/{spec,plan,tasks,prompt}.md   # SDD: un commit por spec
apps/backend/src/main/java/com/ecommerce/{auth,catalog,checkout,shared}/{api,application,domain,infrastructure}
apps/backend/src/main/resources/db/migration/    # Flyway V1__..., seed incluido
apps/frontend/src/app/{core,shared,features/{auth,catalog,cart,checkout}}
docs/{requirements,arquitectura,ia,decisiones,security}.md  docs/prompts/
.agents/skills/  .claude/agents/  scripts/
```

## Convenciones
- Código, clases, archivos y commits en inglés (Conventional Commits: `feat(auth): ... (spec 001)`); docs en español con términos técnicos en inglés.
- Java: `com.ecommerce.<module>.<layer>`; sufijos `*Controller`, `*Service`, `*Repository`, `*Request`/`*Response` (records), `*Mapper`. Inyección por constructor. Cero lógica en controllers.
- SOLID explícito: una responsabilidad por clase, dependencias hacia puertos, extensión por Strategy (no `if/else` por tipo de descuento).
- TypeScript: `strict`, **sin `any`** (si es inevitable, justificación en comentario y en `docs/decisiones.md`); modelos `readonly`; componentes `OnPush`; naming según `angular-developer/references/naming-conventions.md`.
- Migraciones Flyway versionadas; nunca editar una ya aplicada.

## Comandos
```bash
# Backend (desde apps/backend) — Linux/macOS/Git Bash
./mvnw verify                 # tests + JaCoCo (falla si < 80 % LINE o BRANCH); no requiere JWT_SECRET
JWT_SECRET="$(openssl rand -base64 48)" ./mvnw spring-boot:run   # http://localhost:8080 (requiere JWT_SECRET ≥ 32 bytes)
# Frontend (desde apps/frontend)
npm ci
npm run test:coverage         # Jest, umbral 80 % en jest.config.js
npm run lint && npm run build && npm start   # http://localhost:4200
```
**Windows/PowerShell**: `.\mvnw.cmd verify` y `.\mvnw.cmd spring-boot:run` (con `$env:JWT_SECRET = "..."`, ≥ 32 bytes; en CMD, `set JWT_SECRET=...`); `./mvnw` → `.\mvnw.cmd` y el separador `&&` → `;`.
URLs locales: front `http://localhost:4200` · back `http://localhost:8080` (`/actuator/health`) · BD H2 **en memoria** `jdbc:h2:mem:shop` (usuario `sa`, contraseña vacía) · **consola H2** `http://localhost:8080/h2-console` (JDBC `jdbc:h2:mem:shop`, `sa`, vacío; solo local/demo, [DEC-032](docs/decisiones.md)). Instrucciones completas y comandos para generar `JWT_SECRET` en cada SO: [README](README.md).
CI en `.github/workflows/ci.yml`.

## Seguridad
- Sin secretos en el repo: solo `.env.example`. `JWT_SECRET` (≥ 32 bytes) por variable de entorno; los usuarios seed usan contraseñas de **prueba** documentadas en el README.
- Validar todo input (`jakarta.validation`), comprobar rol y propiedad en cada endpoint, no loguear tokens. CORS solo al origen del front.
- Antes de cerrar auth o checkout: `docs/prompts/security-review.md`. Detalle en [docs/security.md](docs/security.md).

## Reglas para la IA
1. Antes de usar una API de una librería, **consulta Context7 o la documentación oficial** (Boot 4 / Jackson 3 / Angular 22 cambian respecto a lo que recuerdas).
2. No inventes versiones, APIs ni comandos.
3. Lee `spec.md` y `plan.md` del spec activo y los skills de la tabla antes de escribir código; da un **plan corto** y espera el OK.
4. Todo cambio lleva tests: casos de borde del enunciado (tope exacto, carrito vacío o corrupto, cupón inexistente o vencido, stock insuficiente).
5. **Bitácora**: al cerrar cada spec usa el skill `ia-log`. Registra solo hechos reales; nunca inventes correcciones a la IA.
6. **Memoria**: actualiza [MEMORY.md](MEMORY.md) al terminar (estado, pendiente, trampas).
7. Un commit por spec, con el trailer `Co-Authored-By` (alimenta el % de código asistido de `docs/ia.md`).

## Definición de terminado (por spec)
- [ ] Criterios de aceptación del `spec.md` cumplidos y `tasks.md` marcado.
- [ ] `./mvnw verify` y/o `npm run test:coverage` en verde, cobertura ≥ 80 %; lint y build sin errores.
- [ ] Sin lógica en controllers/componentes; DTOs en frontera; errores por `ProblemDetail`.
- [ ] `docs/decisiones.md` e `ia-log` actualizados; MEMORY.md actualizado.
- [ ] Revisión con el sub-agente `code-auditor` (y `coverage-guard`); si toca auth o dinero, `security-review`.
- [ ] Commit del spec.

## Skills (`.agents/skills/`)
| Skill | Úsalo cuando |
|---|---|
| `spec-driven-development` | Siempre: flujo spec → plan → tasks → implementar → verificar → commit |
| `project-interview` | Alcance o seguridad poco claros antes de escribir un spec |
| `implement-feature` | Implementar un spec de punta a punta |
| `verify-versions` | Confirmar o actualizar versiones |
| `discount-engine` | Reglas, orden, redondeo y tope de descuentos; cupones |
| `discount-test-matrix` | Generar la matriz de tests del motor y sus casos de borde |
| `spring-backend-conventions` | Escribir o revisar Java |
| `auth-jwt` | Login, registro, refresh, roles, guards, interceptor |
| `backend-testing` | Tests JUnit/Mockito/JaCoCo del backend |
| `frontend-angular-conventions` | Componentes, signals, rutas, servicios, store del carrito |
| `frontend-testing-jest` | Tests Jest y umbral de cobertura |
| `ia-log` | Registrar prompts y correcciones a la IA en `docs/ia.md` |
| `angular-developer` (externo, Google, MIT) | Referencia oficial de Angular; **donde mencione Vitest, aplica Jest** |

## Sub-agentes (`.claude/agents/`, solo Claude Code)
- `code-auditor`: revisa el diff contra SOLID, tipado estricto, `any`, lógica en controllers y eficiencia. Solo lectura.
- `coverage-guard`: corre las suites, valida el 80 % y propone los tests que faltan. Solo lectura de código.

## MCPs
Context7 configurado en `.mcp.json` (docs por versión). Úsalo: "use context7". Requiere aprobación del usuario para instalarse (ver README).
