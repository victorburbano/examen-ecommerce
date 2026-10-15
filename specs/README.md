# Specs (Spec-Driven Development)

Una spec = un commit. Orden de ejecución (cada una depende de las anteriores):

| # | Spec | Alcance | HU | Estado |
|---|---|---|---|---|
| 000 | [foundation](000-foundation/spec.md) | Monorepo, skills, agentes, CI, docs base, esqueletos back/front | — | ✅ |
| 001 | [auth](001-auth/spec.md) | Register, login, refresh, logout, roles, seed de usuarios; login/registro/guards en el front | — | ⛔ |
| 002 | [catalog](002-catalog/spec.md) | Catálogo con stock, seed; lista de productos y carrito con signals | HU 1 | ⛔ |
| 003 | [discount-engine](003-discount-engine/spec.md) | Dominio puro: Strategy + Pipeline + Factory, tope, matriz de tests | (núcleo) | ⛔ |
| 004 | [checkout-backend](004-checkout-backend/spec.md) | Cupones en BD, quote, checkout, stock `@Version`, órdenes | HU 3 | ⛔ |
| 005 | [checkout-frontend](005-checkout-frontend/spec.md) | Cupón, desglose, alerta del tope, checkout, órdenes | HU 2, HU 4 | ⛔ |
| 006 | [docs-governance](006-docs-governance/spec.md) | `arquitectura.md`, `ia.md`, README final, guion de demo | — | ⛔ |

Cada carpeta: `spec.md` (qué) · `plan.md` (cómo) · `tasks.md` (checklist) · `prompt.md` (prompt para ejecutarla).
Flujo detallado: skill `spec-driven-development`.
