---
name: implement-feature
description: Implementa una spec de punta a punta siguiendo AGENTS.md y los skills del proyecto. Úsalo después de que spec.md y plan.md estén aprobados.
---

# implement-feature

1. **Contexto**: `AGENTS.md`, `spec.md`, `plan.md` y los skills del plan (backend: `spring-backend-conventions`, `discount-engine`, `auth-jwt`; front: `frontend-angular-conventions`).
2. **Docs**: Context7 u oficial para cada librería que toques (Boot 4, Jackson 3, Angular 22).
3. **Plan corto** (≤ 10 líneas): clases por capa, contratos, migraciones, tests. Espera el OK.
4. **Tests primero o junto al código** (`backend-testing`, `frontend-testing-jest`, `discount-test-matrix`): felices, bordes y errores.
5. **Implementa** por capas: dominio → aplicación → infraestructura → API. Sin lógica en controllers ni componentes.
6. **Verifica**: `./mvnw verify` y/o `npm run test:coverage`, lint, build.
7. **Revisa tu diff** contra SOLID (¿una responsabilidad?, ¿depende de abstracciones?, ¿if/else por tipo?) y eficiencia; usa el sub-agente `code-auditor`.
8. **Cierra** con `ia-log` (prompt clave y correcciones reales) y actualiza `MEMORY.md`.
