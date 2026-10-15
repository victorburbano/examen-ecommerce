# Prompt 003

```
Implementa la spec specs/003-discount-engine siguiendo spec.md, plan.md y tasks.md, los skills spec-driven-development, discount-engine, discount-test-matrix, spring-backend-conventions y backend-testing, y AGENTS.md.
Antes de codificar, genera con discount-test-matrix la tabla de casos con los valores esperados calculados a mano (no con el código) y muéstramela.
Consulta Context7 para @ConfigurationProperties en Spring Boot 4 antes de usar su API.
Primero un plan corto (≤ 10 líneas) y espera mi OK.
Entrega: dominio puro sin Spring/JPA (Strategy + Pipeline + Factory), configuración en application.yml, tests parametrizados y de borde con ≥ 95 % de ramas en el paquete, docs/arquitectura.md con los patrones realmente implementados.
Al terminar: ./mvnw verify en verde, invoca code-auditor (busca sumas de porcentajes, constantes mágicas, redondeo final único) y coverage-guard, dime qué corregiste y regístralo con ia-log. Commit: "feat(checkout): cumulative discount engine with 35% cap (spec 003)".
```
