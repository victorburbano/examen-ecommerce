# Gobernanza de IA

> **Esqueleto (spec 000).** Se llena con hechos reales al cerrar cada spec (skill `ia-log`). Las secciones marcadas *pendiente* no contienen entradas inventadas a propósito.

Asistentes usados: Claude Code (Anthropic) para el scaffold (spec 000) y OpenCode para la implementación (specs 001 a 005). Método: Spec-Driven Development, un commit por spec.

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

Detalle de `discount-test-matrix`: en la spec 003 se usó para fijar la tabla de casos (12 filas) del motor **antes** de codificar; los valores esperados se calcularon a mano y luego el `@ParameterizedTest` los verificó. Sirvió para destapar un error de cálculo (corrección 6) y para cubrir los bordes exigidos por el enunciado: umbral exacto (100.00 no aplica, 100.01 sí), `HALF_UP` en `.005`, tope alcanzado justo (35.00), tope superado (paso `CAP_ADJUSTMENT` negativo) y orden de reglas (pipeline permutado).

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
| 4 | 2026-10-15 | 003 | Motor de descuentos acumulativos con tope del 35 % (HU 2 y 4) | Plan corto aprobado; el humano pidió **recomendación con razones** sobre el conflicto entre `plan.md` (`category-name: Tecnología`) y DEC-018, y aprobó `app.discounts.category: TECHNOLOGY` + el puente de infraestructura | 195 tests backend (LINE 99.6 %, BRANCH 99.1 %; paquete `domain/discount` al **100 %** de líneas y ramas); `mvnw verify` verde | Se aprobó DEC-020; la tabla parametrizada destapó un error de cálculo a mano y un test mal planteado (correcciones 6 y 7) |
| 5 | 2026-10-15 | 004 | Checkout: `quote` público, `orders` autenticado, cupones en BD, stock con `@Version` y reintento (HU 3) | Plan corto aprobado con 3 recomendaciones de diseño (unidad `@Transactional` + reintento fuera de ella, `PageResult` de dominio, desglose completo con carga en lote) | 290 tests backend (LINE **99.8 %**, BRANCH **99.4 %**); `mvnw verify` verde | Se aprobaron las 3 recomendaciones (DEC-023..025); la propia suite destapó dos fallos en tests nuevos (correcciones 8 y 9) y se remató el flaky heredado (corrección 10) |
| 6 | 2026-10-15 | 005 | Checkout en el front: cupón, desglose, banner del tope, compra y órdenes (HU 2 y HU 4) | Plan corto aprobado; se cambió `resource` por un **store explícito con guarda de secuencia** (DEC-027) en vez del `resource`/`httpResource` que sugería `plan.md`, y se pidió recomendación de diseño sobre cómo distinguir el 409 | 145 tests frontend, **100 %** (stmts/branch/funcs/lines); `npm run lint` y `build` verdes; backend intacto (`mvnw verify` 290 tests, LINE 99.8 %/BRANCH 99.4 %) | Se aprobaron DEC-027..029; `lint` y la propia suite destaparon 5 fallos reales (correcciones 11–15) antes de cerrar |

### Porcentaje de código asistido
Método reproducible (`ia-log`): todos los commits llevan el trailer `Co-Authored-By`, así que se mide el diff del commit de cada spec con `git show --shortstat <commit>` y `git show --numstat <commit>`.

Spec 001 — líneas de **código** (cifra estable, independiente de la redacción de estos docs): producción **1941** · tests **2311** · config/assets **368**; 106 archivos en total (incluidos docs y specs).
**Código asistido por IA: 100 % de las líneas de código del diff** (borrador + tests generados por la IA bajo specs aprobadas). El humano no escribió líneas de implementación en esta spec: aportó las decisiones de arquitectura y criterios (DEC-001 cap configurable, DEC-010 auth completo, DEC-012 `quote` público) y aprobó el plan antes de codificar; la revisión de cierre se hizo contra `docs/security.md` y las convenciones.

Spec 002 — líneas de **código** del diff: producción **1045** (Java main 349 · TS de componentes/servicios 254 · plantillas y CSS 421 · migraciones SQL 21) · tests **932** (Java 356 · specs Jest 576); 47 líneas de docs/specs. 49 archivos.
**Código asistido por IA: 100 % de las líneas de código del diff**. El humano fijó los criterios de aceptación del `spec.md`, pidió y aprobó tres recomendaciones de diseño (DEC-017..019) y validó el plan corto antes de escribir código; la lógica de negocio del carrito (tope de stock, céntimos enteros) se revisó contra los casos de borde del enunciado.

Spec 003 — líneas de **código** del diff: producción **543** (Java main: dominio puro 494 + infra de config 49) · tests **870** (Java) · config **13** (`application.yml` principal y de test); 71 líneas de docs/specs (incluye este `ia.md` y `MEMORY.md`). 41 archivos.
**Código asistido por IA: 100 % de las líneas de código del diff**. El humano pidió una recomendación con razones sobre el conflicto entre `plan.md` y DEC-018 (categoría en config) y aprobó `app.discounts.category: TECHNOLOGY` + el puente de infraestructura (DEC-020) antes de codificar; los valores esperados del motor se calcularon **a mano** y el humano revisó el invariante "suma de pasos = descuento total".

Spec 004 — líneas de **código** del diff: producción **1569** (Java main 1512 + migraciones SQL `V5`/`V6` 57) · tests **2105** (Java) · docs/specs **89** (incluye `decisiones.md`, `security.md`, `ia.md` y `MEMORY.md`). 77 archivos.
**Código asistido por IA: 100 % de las líneas de código del diff**. El humano fijó los criterios de aceptación del `spec.md` (endpoints, códigos de error, seed de cupones, criterios de concurrencia/rollback) y aprobó tres recomendaciones de diseño (DEC-023 unidad transaccional + reintento, DEC-024 `PageResult` de dominio, DEC-025 desglose completo sin N+1) antes de codificar; los importes esperados de las cotizaciones (con y sin tope) se calcularon **a mano** y se verificaron contra el motor real en los tests de integración.

Spec 005 — líneas de **código** del diff: producción **1223** (TS de store/servicios/componentes 544 · plantillas 225 · CSS 454) · tests **1286** (specs Jest) · docs/specs aparte (`decisiones.md`, `security.md`, `ia.md`, `MEMORY.md`, `specs/005`). 42 archivos.
**Código asistido por IA: 100 % de las líneas de código del diff**. El humano fijó los criterios del `spec.md` (texto exacto del banner, estados visibles, casos de borde: cupón aplicado al cambiar el carrito, doble clic, respuesta obsoleta) y aprobó el cambio de diseño frente al `plan.md`: **store explícito con guarda de secuencia** en vez de `resource`/`httpResource` (DEC-027), y el modo de distinguir el 409 (DEC-028). La verificación manual con el backend en marcha (`WELCOME2026` → 3 líneas sin tope; `MEGA40` → 35.00 % con `capApplied`) se contrastó contra el motor real, no contra valores escritos a mano.

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

#### 6. Porcentaje efectivo mal calculado a mano en la matriz de tests
- **Sugerencia original** (spec 003): en la fila del caso "HALF_UP en `.005`" (`Hogar` por 100.10) la matriz esperaba un descuento de **5.01 %** sobre el subtotal.
- **Problema técnico**: el importe del descuento es 5.01 (redondeo `HALF_UP` de 5.005), pero el **porcentaje efectivo** es `5.01 × 100 / 100.10 = 5.00499… %` → `HALF_UP` a escala 2 da **5.00 %**, no 5.01. El error estaba en el valor esperado escrito a mano, no en el motor.
- **Corrección aplicada**: fijar la expectativa en 5.00 % (el importe 5.01 y el total 95.09 siguen igual).
- **Evidencia**: `DiscountPipelineTest` (fila "half up on .005 in the volume step") y `MoneyTest.expressesARatioAsAPercent`; `./mvnw verify` en verde.

#### 7. Test "no instanciable" contra una suposición falsa del propio código
- **Sugerencia original** (spec 003): un test `MoneyTest.isNotInstantiable` que forzaba la reflexión sobre el constructor privado de `Money` y esperaba una `UnsupportedOperationException`.
- **Problema técnico**: `Money` tiene un constructor privado **vacío** (idiom de clase de utilidades), así que `newInstance()` no lanza nada y el test falla con "Expecting code to raise a throwable". El test asumía un comportamiento que el código no tenía.
- **Corrección aplicada**: eliminar el test (no hay nada que aseverar en un constructor privado vacío) y quitar el `assertThatThrownBy` que quedaba sin uso.
- **Evidencia**: `apps/backend/src/test/java/com/ecommerce/checkout/domain/discount/MoneyTest.java`; `./mvnw verify` en verde (195 tests).

#### 8. La validación de los records lanza `NullPointerException`, no `IllegalArgumentException`
- **Sugerencia original** (spec 004): en los tests de dominio de `Coupon`, `Order`, `OrderItem`, `OrderDiscount` y `PageResult` se asertó `assertThatIllegalArgumentException()` para los campos `null`.
- **Problema técnico**: los constructores compactos usan `Objects.requireNonNull(...)`, que lanza **`NullPointerException`**, así que 5 tests fallaban con "...but was: NullPointerException". La expectativa era incorrecta, no el código.
- **Corrección aplicada**: distinguir `assertThatNullPointerException()` (nulos) de `assertThatIllegalArgumentException()` (vacío/negativo/rango).
- **Evidencia**: `CouponTest`, `OrderTest`, `OrderItemTest`, `OrderDiscountTest`, `PageResultTest`; `./mvnw verify` en verde.

#### 9. Índice del paso de tope mal contado en el test de integración
- **Sugerencia original** (spec 004): en el caso `MEGA40`, asertar `$.discounts[2].type == CAP_ADJUSTMENT`.
- **Problema técnico**: el desglose lleva **cuatro** pasos (categoría, volumen, cupón y tope), así que el ajuste por tope es el índice **3**; el 2 es `COUPON`. El test falló con `expected:<CAP_ADJUSTMENT> but was:<COUPON>` aunque el tope sí se aplicaba (`effectiveDiscountPercent = 35.00`).
- **Corrección aplicada**: asertar el índice 3 (y además que `discounts[3].amount = -34.12`).
- **Evidencia**: `CheckoutIntegrationTest.quotesMega40WithTheCapApplied`; `./mvnw verify` en verde.

#### 10. El test flaky del token manipulado seguía vivo en la suite de unidad (`JwtIssuerTest`)
- **Contexto**: la corrección 4 arregló `AuthFlowIntegrationTest`, pero el mismo patrón frágil (`token.substring(0, len-1) + (endsWith('A') ? 'B' : 'A')`) seguía en `JwtIssuerTest`, que volvió a fallar de forma intermitente al correr `./mvnw verify` de la spec 004.
- **Problema técnico**: idéntico al de la corrección 4: cambiar el último carácter base64url de la firma no siempre la invalida (32 bytes → 43 chars, el último con 2 bits útiles), así que el token "manipulado" a veces seguía siendo válido.
- **Corrección aplicada**: manipular el **payload** (primer carácter del segundo segmento) también en `JwtIssuerTest`.
- **Evidencia**: `apps/backend/src/test/java/com/ecommerce/auth/infrastructure/JwtIssuerTest.java`; `./mvnw verify` en verde y estable.

#### 11. `output<void>()` prohibido por el linter (`no-invalid-void-type`)
- **Sugerencia original** (spec 005): declarar las salidas del panel como `readonly ordered = output<void>();` para tiparlas explícitamente.
- **Problema técnico**: igual que la corrección 3 pero en `output`: `@typescript-eslint/no-invalid-void-type` (config `strict`) rechaza `void` como argumento de tipo en una **llamada** (`output<void>()`). `npm run lint` falló con 3 errores en `checkout-panel.ts` y `order-confirmation.ts`.
- **Corrección aplicada**: `output()` sin genérico (por defecto `void`), con `emit()` sin argumentos.
- **Evidencia**: `apps/frontend/src/app/features/checkout/checkout-panel/checkout-panel.ts:30`, `.../order-confirmation/order-confirmation.ts:16`; `npm run lint` en verde.

#### 12. El test de compra ponía la sesión en un store obsoleto (orden de inicialización)
- **Sugerencia original** (spec 005): en `checkout-panel.spec.ts`, `auth.setAccessToken('jwt')` **antes** de `createWithQuote()`.
- **Problema técnico**: la variable `auth` se reasigna **dentro** de `create()`/`createWithQuote()`, que corre después; el token se ponía en el `AuthStore` de un test anterior, así que el panel veía `isAuthenticated() === false`, redirigía a `/login` y el test fallaba con `Expected one matching request for criteria "Match URL: /api/checkout/orders", found none` (el fallo se detapó al correr la suite completa).
- **Corrección aplicada**: crear el fixture primero y poner el token después (el `AuthStore` es `providedIn: 'root'`, la misma instancia que inyecta el componente).
- **Evidencia**: `apps/frontend/src/app/features/checkout/checkout-panel/checkout-panel.spec.ts`; `npm run test:coverage` en verde (145 tests).

#### 13. `create()` invocado dos veces en el mismo test (`TestBed` ya instanciado)
- **Sugerencia original** (spec 005): reutilizar el mismo helper `create()` dos veces dentro de un `it` para comparar dos estados (banner visible/oculto y tope sí/no).
- **Problema técnico**: `TestBed.configureTestingModule` no se puede volver a llamar una vez instanciado el módulo; los tests fallaban con "Cannot configure the test module when the test module has already been instantiated".
- **Corrección aplicada**: un `it` por caso (o `TestBed.resetTestingModule()` antes del segundo `create()`).
- **Evidencia**: `apps/frontend/src/app/features/checkout/cap-alert/cap-alert.spec.ts` y `.../order-confirmation/order-confirmation.spec.ts`; suite en verde.

#### 14. `ReadonlyArray<T>` prohibido por la regla `array-type`
- **Sugerencia original** (spec 005): `const cases: ReadonlyArray<readonly [string, string]> = [...]` en el test del mapper de errores.
- **Problema técnico**: `@typescript-eslint/array-type` (config `strict`) exige la forma `readonly T[]` para los arrays de solo lectura. `npm run lint` falló.
- **Corrección aplicada**: `readonly (readonly [string, string])[]`.
- **Evidencia**: `apps/frontend/src/app/features/checkout/checkout-error.spec.ts`; `npm run lint` en verde.

#### 15. Rutas de import mal resueltas al crear `OrdersPage`
- **Sugerencia original** (spec 005): en `orders-page/orders-page.ts`, `import { Order } from '../checkout/checkout.models'` y `import { OrdersApiService } from './orders-api.service'`.
- **Problema técnico**: el componente está en `features/orders/orders-page/`, así que `../checkout/...` apuntaba a `features/orders/checkout/...` y `./orders-api.service` al archivo dentro de `orders-page/`; el servicio está en `features/orders/`. `ng build` falló con `TS2307: Cannot find module`.
- **Corrección aplicada**: `../../checkout/checkout.models` y `../orders-api.service` (verificar la profundidad real del árbol, no de memoria).
- **Evidencia**: `apps/frontend/src/app/features/orders/orders-page/orders-page.ts`; `npm run build` en verde.
