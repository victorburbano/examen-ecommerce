# MEMORY.md — Estado del proyecto y continuidad

> `AGENTS.md` es la fuente de verdad de convenciones; este archivo registra **qué ya está hecho**, **qué falta** y **qué no hay que volver a decidir**.
> **Al empezar una sesión**: léelo. **Al terminar**: actualiza "Estado", "Pendiente" y "Bitácora de sesiones".

- **Última actualización**: 2026-10-15
- **Estado global**: specs 000 y 001 completas; specs 002–006 pendientes.

## 1. Estado por spec
| Spec | Estado | Notas |
|---|---|---|
| 000 foundation | ✅ | Esqueletos back y front verificados; commit inicial |
| 001 auth | ✅ | Implementada (back + front) y verificada; pendiente solo el commit |
| 002 catalog | ⛔ | Siguiente. |
| 003 discount-engine | ⛔ | Núcleo evaluable: valores esperados a mano |
| 004 checkout-backend | ⛔ | |
| 005 checkout-frontend | ⛔ | |
| 006 docs-governance | ⛔ | `arquitectura.md`, `ia.md` finales, README, demo |

**Verificación de la spec 001 (2026-10-15)**
- Backend: `./mvnw verify` → BUILD SUCCESS. **82 tests**; JaCoCo LINE **99.4 %** (332/334), BRANCH **97.5 %** (39/40).
- Frontend: `npm run test:coverage` → **46 tests, 100 %** (stmts/branch/funcs/lines); `npm run lint` y `npm run build` OK.
- Endpoints: `POST /api/auth/{register,login,refresh,logout}` y `GET /api/auth/me`.

**Verificación del esqueleto (2026-10-15)**
- Backend: `./mvnw verify` → BUILD SUCCESS (JDK 25 local, Boot 4.1.1; JaCoCo sin clases contables).
- Frontend: `npm run lint`, `npm run test:coverage` (1 test, 100 %) y `npm run build` OK.
- CI (`.github/workflows/ci.yml`) **no ejecutado todavía** (no hay remoto).

## 2. Decisiones cerradas (no re-discutir)
Ver [docs/decisiones.md](docs/decisiones.md) (DEC-001..016). Resumen: tope del 35 % configurable y alcanzable con `MEGA40` · cupones en BD solo validación · H2 + Flyway · `@Version` para stock · sin WebSocket · backend única fuente de cálculo · HALF_UP por paso · sin `packages/` · CSS propio · auth completo con roles · `quote` público y `orders` autenticado · paquete `com.ecommerce` · refresh opaco rotado con hash y `noRollbackFor` · Signal Forms + access token solo en memoria · CSRF deshabilitado con justificación.

## 3. Trampas conocidas
- **Hallazgo**: con las reglas del enunciado el descuento máximo es 27.325 % → el tope no se alcanza con `WELCOME2026` (DEC-001).
- **Boot 4.1 renombra los paquetes de test**: `@WebMvcTest`/`@AutoConfigureMockMvc` están en `org.springframework.boot.webmvc.test.autoconfigure`, `@DataJpaTest` en `org.springframework.boot.data.jpa.test.autoconfigure`, `@MockitoBean` en `org.springframework.test.context.bean.override.mockito`. Verificar en los jars de `~/.m2` antes de importar de memoria.
- **Auth**: `@PreAuthorize` lanza `AccessDeniedException` dentro del controlador → el 403 hay que mapearlo en el `GlobalExceptionHandler`, no solo en el filter chain. Y el refresh necesita `noRollbackFor` para no deshacer la revocación de familia (DEC-014).
- **Refresh token en tests**: `JwtIssuerTest` usa un `Clock` fijo basado en "ahora" (truncado a segundos) porque el decoder valida la expiración contra el reloj real.
- Spring Security 7 añade la authority `FACTOR_BEARER` junto a las `ROLE_*`: **no** asertar el conjunto exacto de authorities en los tests.
- Angular 22 exige TypeScript `>=6.0 <6.1`: **no** instalar TS 7.x.
- **eslint `no-invalid-void-type`**: `void` no vale como argumento de tipo en una llamada (`post<void>`); en posición de retorno sí. Usar `post<undefined>` si hace falta.
- Jest, no Vitest (Angular usa Vitest por defecto); `jest.config.js` en CommonJS. `providers` zoneless globales vía `setupZonelessTestEnv`; no añadir `provideZonelessChangeDetection()` en los TestBed.
- **Navegación en tests de interceptores**: `expireSession()` navega a `/login`; espiar `Router.navigate` para no romper el spec.
- Los symlinks de `.claude/skills/` requieren Modo desarrollador en Windows; usa `scripts/sync-agent-links.ps1`.
- Migraciones aplicadas/planificadas: V1 auth · V2 seed users · V3 catalog · V4 seed products · V5 checkout · V6 seed coupons. No editar una aplicada.
- No uses `rm` con globs relativos en la shell de Claude (el sandbox lo bloquea); usa rutas absolutas.

## 4. Pendiente (no vinculado a una spec)
- Commit de la spec 001 (mensaje `feat(auth): register, login, refresh rotation and roles (spec 001)`).
- Aprobar e instalar Context7 (`resources` vacío ⇒ docs oficiales vía webfetch).
- Crear el repo remoto público en GitHub y ejecutar el CI una vez.
- Rellenar `docs/ia.md` con correcciones **reales** conforme ocurran (`ia-log`).

## 5. Bitácora de sesiones
| Fecha | Qué se hizo |
|---|---|
| 2026-10-15 | Entrevista (15 preguntas), verificación de versiones, propuesta aprobada, scaffold completo (spec 000): skills, agentes, docs, specs 001–006, esqueletos verificados |
| 2026-10-15 | Spec 001 completa: backend auth (JWT Nimbus, refresh rotado con detección de reutilización, roles) + frontend (store, interceptor con refresh único, guards, Signal Forms, cabecera); 82 tests backend (99.4 %/97.5 %) y 46 frontend (100 %); docs de cierre y `ia.md` actualizados |
