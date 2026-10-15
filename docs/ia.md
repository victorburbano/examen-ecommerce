# Gobernanza de IA

> **Esqueleto (spec 000).** Se llena con hechos reales al cerrar cada spec (skill `ia-log`). Las secciones marcadas *pendiente* no contienen entradas inventadas a propósito.

Asistentes usados: Claude Code (Anthropic) para el scaffold (spec 000) y OpenCode para la implementación (specs 001 y 002). Método: Spec-Driven Development, un commit por spec.

## 1. Skills / prompts automatizados
| Skill | Qué automatiza | Dónde |
|---|---|---|
| `spec-driven-development` | Flujo spec → plan → tasks → implementar → verificar → commit | `.agents/skills/spec-driven-development` |
| `discount-engine` | Reglas, orden, redondeo y tope del motor; evita errores típicos (sumar % en vez de cascada) | `.agents/skills/discount-engine` |
| `discount-test-matrix` | Genera la matriz de tests parametrizados del motor con los casos de borde del enunciado; los esperados se calculan a mano | `.agents/skills/discount-test-matrix` |
| `auth-jwt` | Diseño de auth (access/refresh, roles, guards, interceptor); fuente de las decisiones de la spec 001 | `.agents/skills/auth-jwt` |
| `spring-backend-conventions` · `backend-testing` | Capas/DTOs/errores del backend y estrategia JUnit 5 + Mockito + JaCoCo | `.agents/skills/` |
| `frontend-angular-conventions` · `frontend-testing-jest` | Standalone/signals/rutas del front y tests Jest (anula Vitest) | `.agents/skills/` |
| `implement-feature` | Implementar una spec de punta a punta tras aprobar `plan.md` | `.agents/skills/implement-feature` |
| Prompts por spec | Un `prompt.md` por spec con alcance, criterios y entrega | `specs/NNN-*/prompt.md` |

Detalle de `discount-test-matrix`: *pendiente* (ejemplo de uso real al cerrar la spec 003).

## 2. Sub-agentes
| Agente | Rol y reglas | Archivo |
|---|---|---|
| `code-auditor` | Solo lectura. Revisa el diff: SOLID, capas, `any`, dinero con `double`, constantes mágicas, N+1, seguridad. Marca **[ia-log]** lo que debe registrarse | `.claude/agents/code-auditor.md` |
| `coverage-guard` | Corre las suites, exige ≥ 80 % LINE/BRANCH y revisa los casos de borde. No baja umbrales ni excluye archivos | `.claude/agents/coverage-guard.md` |

No disponibles fuera de Claude Code: en la spec 001 se hizo la revisión equivalente a mano (diff contra SOLID, tipado sin `any`, cero lógica en controladores, umbrales verificados). Primer hallazgo real registrado en la sección 3 (corrección 1).

## 3. Bitácora de co-creación
### Prompts clave
| # | Fecha | Spec | Objetivo | Prompt | Resultado | Verificación humana |
|---|---|---|---|---|---|---|
| 1 | 2026-10-15 | 000 | Entender el enunciado, proponer arquitectura y specs | Conversación de entrevista (Fase 0) | Decisiones DEC-001..011 | El humano corrigió: auth completo, cupones en BD, cap configurable |
| 2 | 2026-10-15 | 001 | Implementar auth de punta a punta (backend + frontend) | Plan corto aprobado; skills `implement-feature` + `auth-jwt` | 82 tests backend (LINE 99.4 %, BRANCH 97.5 %) y 46 tests frontend (100 %); `mvnw verify`, lint y build verdes | El humano aprobó el plan corto; los tests de integración destaparon dos fallos (correcciones 1 y 2) |
| 3 | 2026-10-15 | 002 | Catálogo público + carrito reactivo (HU 1) | Plan corto aprobado; el humano pidió **recomendación con razones** sobre 3 dudas de diseño (formato del precio, categoría en el wire, dinero en el front) antes de codificar | 104 tests backend (LINE 99.5 %, BRANCH 98.1 %) y 90 frontend (100 %); `mvnw verify`, lint y build verdes | Se aprobaron las 3 recomendaciones (DEC-017..019); la suite destapó un test flaky heredado y dos fallos en las specs nuevas (correcciones 4 y 5) |

### Porcentaje de código asistido
Método reproducible (`ia-log`): todos los commits llevan el trailer `Co-Authored-By`, así que se mide el diff del commit de cada spec con `git show --shortstat <commit>` y `git show --numstat <commit>`.

Spec 001 — líneas de **código** (cifra estable, independiente de la redacción de estos docs): producción **1941** · tests **2311** · config/assets **368**; 106 archivos en total (incluidos docs y specs).
**Código asistido por IA: 100 % de las líneas de código del diff** (borrador + tests generados por la IA bajo specs aprobadas). El humano no escribió líneas de implementación en esta spec: aportó las decisiones de arquitectura y criterios (DEC-001 cap configurable, DEC-010 auth completo, DEC-012 `quote` público) y aprobó el plan antes de codificar; la revisión de cierre se hizo contra `docs/security.md` y las convenciones.

Spec 002 — líneas de **código** del diff: producción **1045** (Java main 349 · TS de componentes/servicios 254 · plantillas y CSS 421 · migraciones SQL 21) · tests **932** (Java 356 · specs Jest 576); 47 líneas de docs/specs. 49 archivos.
**Código asistido por IA: 100 % de las líneas de código del diff**. El humano fijó los criterios de aceptación del `spec.md`, pidió y aprobó tres recomendaciones de diseño (DEC-017..019) y validó el plan corto antes de escribir código; la lógica de negocio del carrito (tope de stock, céntimos enteros) se revisó contra los casos de borde del enunciado.

### Correcciones a sugerencias de la IA (mínimo 2, reales)
#### 1. El refresh revocaba la familia pero el rollback del 401 deshacía la revocación
- **Sugerencia original**: `RefreshService.refresh` con `@Transactional` simple.
- **Problema técnico**: al detectar reutilización se hace `revokeFamily(...)` y luego se lanza `InvalidRefreshTokenException`; el `@Transactional` la marcaba como *rollback-only* y **deshacía el UPDATE**, así que la familia robada seguía activa.
- **Corrección aplicada**: `@Transactional(noRollbackFor = InvalidRefreshTokenException.class)`.
- **Evidencia**: `apps/backend/src/main/java/com/ecommerce/auth/application/RefreshService.java:50`; test de reutilización en `AuthFlowIntegrationTest` (verifica que el segundo uso del mismo token y el uso del token rotado devuelven 401 y revocan la familia).

#### 2. El 403 de `@PreAuthorize` no pasaba por los handlers de Spring Security
- **Sugerencia original**: confiar el 403 al `ProblemDetailAccessDeniedHandler` registrado en el filter chain.
- **Problema técnico**: `@PreAuthorize` lanza `AccessDeniedException` **dentro del controlador**, después del filter chain, así que la capturaba el `@RestControllerAdvice` y el cliente recibía un 500 por el handler genérico.
- **Corrección aplicada**: handler `AccessDeniedException` → `403 FORBIDDEN` en el advice.
- **Evidencia**: `apps/backend/src/main/java/com/ecommerce/shared/error/GlobalExceptionHandler.java:55`; `RoleBasedAccessIntegrationTest` (sondeo de rol: `USER` sobre endpoint de `ADMIN` devuelve 403).

#### 3. `post<void>` prohibido por el linter del frontend
- **Sugerencia original**: `logout(): Observable<void> { return this.http.post<void>('/api/auth/logout', null, ...); }`.
- **Problema técnico**: `@typescript-eslint/no-invalid-void-type` prohíbe `void` como argumento de tipo en una `CallExpression` (`post<void>(...)`), aunque sí lo permite en posición de retorno.
- **Corrección aplicada**: `post<undefined>(...)` manteniendo la firma pública `Observable<void>`.
- **Evidencia**: `apps/frontend/src/app/core/auth/auth-api.service.ts`; `npm run lint` en verde.

#### 4. El test de "token manipulado" era flaky: podía no invalidar la firma
- **Sugerencia original** (spec 001): para probar que un token manipulado se rechaza, cambiar su **último** carácter (`token.substring(0, len-1) + (endsWith('A') ? 'B' : 'A')`).
- **Problema técnico**: una firma HS256 son 32 bytes = **43 chars base64url**, y el último carácter solo codifica **2 bits útiles** de los 6 que aporta; los caracteres cuya parte significativa coincide (`A`/`Q`/`g`/`w`) decodifican a la **misma** firma, así que el token "manipulado" seguía siendo válido y el test devolvía 200 en lugar de 401 (~6 % de las ejecuciones). Apareció al correr la suite de la spec 002.
- **Corrección aplicada**: manipular el **payload** (primer carácter del segundo segmento), que siempre cambia el contenido firmado.
- **Evidencia**: `apps/backend/src/test/java/com/ecommerce/auth/api/AuthFlowIntegrationTest.java` (`tamperPayload`); `./mvnw verify` en verde.

#### 5. El selector CSS del panel se rompía con un nombre que contiene comillas
- **Sugerencia original** (spec 002): localizar botones con `querySelector('button[aria-label="..."]')` construido a partir del nombre del producto.
- **Problema técnico**: un nombre seed real como `Monitor 27"` contiene una comilla doble, y el selector resultante (`button[aria-label="Añadir una unidad de Monitor 27""]`) es **inválido** (`SyntaxError` en jsdom). Los datos de prueba realistas rompen la técnica del helper.
- **Corrección aplicada**: buscar el botón recorriendo `querySelectorAll('button')` y comparando `getAttribute('aria-label')` por igualdad (sin interpolación en el selector).
- **Evidencia**: `apps/frontend/src/app/features/cart/cart-panel/cart-panel.spec.ts`; `npm run test:coverage` en verde (90 tests).
