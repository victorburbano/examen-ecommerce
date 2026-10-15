# MEMORY.md — Estado del proyecto y continuidad

> `AGENTS.md` es la fuente de verdad de convenciones; este archivo registra **qué ya está hecho**, **qué falta** y **qué no hay que volver a decidir**.
> **Al empezar una sesión**: léelo. **Al terminar**: actualiza "Estado", "Pendiente" y "Bitácora de sesiones".

- **Última actualización**: 2026-10-15
- **Estado global**: spec 000 completa; specs 001–006 pendientes.

## 1. Estado por spec
| Spec | Estado | Notas |
|---|---|---|
| 000 foundation | ✅ | Esqueletos back y front verificados; commit inicial |
| 001 auth | ⛔ | Siguiente. Prompt en `specs/001-auth/prompt.md` |
| 002 catalog | ⛔ | |
| 003 discount-engine | ⛔ | Núcleo evaluable: valores esperados a mano |
| 004 checkout-backend | ⛔ | |
| 005 checkout-frontend | ⛔ | |
| 006 docs-governance | ⛔ | `arquitectura.md`, `ia.md` finales, README, demo |

**Verificación del esqueleto (2026-10-15)**
- Backend: `./mvnw verify` → BUILD SUCCESS (JDK 25 local, Boot 4.1.1; JaCoCo sin clases contables).
- Frontend: `npm run lint`, `npm run test:coverage` (1 test, 100 %) y `npm run build` OK.
- CI (`.github/workflows/ci.yml`) **no ejecutado todavía** (no hay remoto).

## 2. Decisiones cerradas (no re-discutir)
Ver [docs/decisiones.md](docs/decisiones.md) (DEC-001..013). Resumen: tope del 35 % configurable y alcanzable con `MEGA40` · cupones en BD solo validación · H2 + Flyway · `@Version` para stock · sin WebSocket · backend única fuente de cálculo · HALF_UP por paso · sin `packages/` · CSS propio · auth completo con roles · `quote` público y `orders` autenticado · paquete `com.ecommerce`.

## 3. Trampas conocidas
- **Hallazgo**: con las reglas del enunciado el descuento máximo es 27.325 % → el tope no se alcanza con `WELCOME2026` (DEC-001).
- Angular 22 exige TypeScript `>=6.0 <6.1`: **no** instalar TS 7.x.
- Jest, no Vitest (Angular usa Vitest por defecto); `jest.config.js` en CommonJS.
- Los symlinks de `.claude/skills/` requieren Modo desarrollador en Windows; usa `scripts/sync-agent-links.ps1`.
- Migraciones planificadas: V1 auth · V2 seed users · V3 catalog · V4 seed products · V5 checkout · V6 seed coupons. No editar una aplicada.
- No uses `rm` con globs relativos en la shell de Claude (el sandbox lo bloquea); usa rutas absolutas.

## 4. Pendiente (no vinculado a una spec)
- Aprobar e instalar Context7: `claude mcp add context7 -- npx -y @upstash/context7-mcp@latest`.
- Crear el repo remoto público en GitHub y ejecutar el CI una vez.
- Rellenar `docs/ia.md` con correcciones **reales** conforme ocurran (`ia-log`).

## 5. Bitácora de sesiones
| Fecha | Qué se hizo |
|---|---|
| 2026-10-15 | Entrevista (15 preguntas), verificación de versiones, propuesta aprobada, scaffold completo (spec 000): skills, agentes, docs, specs 001–006, esqueletos verificados |
