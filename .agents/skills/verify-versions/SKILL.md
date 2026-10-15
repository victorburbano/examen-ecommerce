---
name: verify-versions
description: Confirma versiones estables actuales del stack (web + Context7) y las registra. Úsalo antes de actualizar dependencias o si el usuario pide "última versión".
---

# verify-versions

1. Para Java, Spring Boot, Angular, TypeScript, Node, Jest, jest-preset-angular y JaCoCo busca la **versión estable actual** en fuentes oficiales: docs.spring.io (system requirements), Maven Central (`maven-metadata.xml`), `registry.npmjs.org/<pkg>/latest`, angular.dev/reference/versions. Complementa con Context7.
2. Distingue estable de milestone/RC/beta. No uses versiones de memoria ni blogs desactualizados.
3. Comprueba compatibilidad cruzada: Angular ↔ TypeScript ↔ Node; `jest-preset-angular` ↔ `@angular/core` (peer deps); Boot ↔ Java.
4. Actualiza la tabla de `AGENTS.md` (con fecha), `apps/backend/pom.xml` y `apps/frontend/package.json`.
5. Si algo no se pudo verificar, dilo en la tabla.

Útil: `npm view <pkg> version peerDependencies`.
