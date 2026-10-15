---
name: spec-driven-development
description: Flujo Spec-Driven Development del proyecto - spec, plan, tasks, implementación, verificación y un commit por spec. Úsalo siempre que se pida construir o cambiar funcionalidad.
---

# spec-driven-development

La spec es el contrato; el código es su consecuencia. Una spec = un commit.

## Estructura de `specs/NNN-nombre/`
| Archivo | Contenido |
|---|---|
| `spec.md` | Qué y por qué: objetivo, historias (HU), reglas, criterios de aceptación Given/When/Then, contrato de API/UI, casos de borde, fuera de alcance |
| `plan.md` | Cómo: diseño técnico, archivos/clases por capa, patrones, migraciones, estrategia de tests |
| `tasks.md` | Checklist ordenado y verificable; se marca al avanzar |
| `prompt.md` | Prompt listo para pegar que ejecuta la spec |

## Flujo
1. **Leer**: `AGENTS.md`, `MEMORY.md`, `spec.md` y `plan.md` del spec activo, y los skills que indica el plan.
2. **Ambigüedad**: si el spec deja huecos, pregunta (`project-interview`) y **actualiza el spec** antes de codificar. El spec manda sobre tu criterio.
3. **Plan corto** (≤ 10 líneas) y espera el OK del usuario.
4. **Implementar** siguiendo `tasks.md`: tests junto al código, capas dominio → aplicación → infraestructura → API.
5. **Verificar**: `./mvnw verify` y/o `npm run test:coverage`, lint, build. Cobertura ≥ 80 %.
6. **Auditar**: sub-agentes `code-auditor` y `coverage-guard`; corrige hallazgos.
7. **Cerrar**: marca `tasks.md`, actualiza `docs/decisiones.md`, `MEMORY.md` e `ia-log` (solo hechos reales).
8. **Commit**: `feat(<módulo>): <resumen> (spec NNN)` con el trailer `Co-Authored-By` indicado por el entorno.

## Reglas
- No implementes nada fuera de `spec.md`. Si aparece algo necesario, añádelo al spec primero.
- Cambios de contrato (endpoints/DTO) se reflejan en el spec en el mismo commit.
- Si descubres que el spec estaba mal, corrígelo y anótalo en `docs/decisiones.md`.

## Anti-patrones
Código sin spec, spec escrita después, commits que mezclan specs, criterios de aceptación no verificables.
