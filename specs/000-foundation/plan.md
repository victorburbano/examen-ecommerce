# Plan 000

- Monorepo `apps/backend` (Maven, `com.ecommerce`) y `apps/frontend` (Angular 22 + Jest), `docs/`, `specs/`, `scripts/`.
- Backend: `spring-boot-starter-parent 4.1.1`, `java.version=25`; starters webmvc, data-jpa, security, oauth2-resource-server, validation, actuator, flyway; H2 runtime; tests: webmvc-test, data-jpa-test, security-test. JaCoCo 0.8.15 con `check` (LINE y BRANCH ≥ 0.80). Maven wrapper 3.9.11.
- Frontend: reutiliza la base verificada de `wallet-bank` (strict TS, zoneless, Jest 30 + jest-preset-angular 17, ESLint) con carpetas `core`, `shared`, `features/{auth,catalog,cart,checkout}`.
- IA: skills propios + `angular-developer` (aprobado), sub-agentes en `.claude/agents/`, symlinks vía `scripts/sync-agent-links.*`.
- Verificación: build y tests de ambos lados; commit único.
