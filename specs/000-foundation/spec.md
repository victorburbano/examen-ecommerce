# Spec 000 — Foundation

## Objetivo
Dejar el monorepo listo para construir con IA: convenciones, skills, sub-agentes, specs, docs base, CI y esqueletos de backend y frontend que compilan y pasan sus verificaciones, **sin lógica de negocio**.

## Criterios de aceptación
- **Given** el repo recién clonado, **When** corro `./mvnw verify` en `apps/backend`, **Then** el build termina en éxito (Java 25, Boot 4.1.1, JaCoCo configurado en LINE/BRANCH ≥ 80 %).
- **Given** el repo, **When** corro `npm ci && npm run lint && npm run test:coverage && npm run build` en `apps/frontend`, **Then** todo pasa con cobertura ≥ 80 %.
- `AGENTS.md` es la fuente única; `CLAUDE.md` solo importa `@AGENTS.md`; los 13 skills están en `.agents/skills/` con symlinks relativos en `.claude/skills/`.
- Existen `docs/{requirements,decisiones,arquitectura,ia,security}.md`, plantillas en `docs/prompts/` y las specs 001–006.
- CI en `.github/workflows/ci.yml` ejecuta ambos jobs.
- Un único commit inicial `chore: scaffold monorepo (spec 000)`.

## Fuera de alcance
Cualquier endpoint, entidad, pantalla o migración de negocio.
