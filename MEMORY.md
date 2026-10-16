# MEMORY.md — Estado del proyecto y continuidad

> `AGENTS.md` es la fuente de verdad de convenciones; este archivo registra **qué ya está hecho**, **qué falta** y **qué no hay que volver a decidir**.
> **Al empezar una sesión**: léelo. **Al terminar**: actualiza "Estado", "Pendiente" y "Bitácora de sesiones".

- **Última actualización**: 2026-10-15
- **Estado global**: specs 000–006 completas; CI no ejecutado aún (sin remoto).

## 1. Estado por spec
| Spec | Estado | Notas |
|---|---|---|
| 000 foundation | ✅ | Esqueletos back y front verificados; commit inicial |
| 001 auth | ✅ | Implementada (back + front), verificada y commiteada (`5a8a4f5`) |
| 002 catalog | ✅ | Backend + frontend, verificados y commiteados (`b3d2aa3`) |
| 003 discount-engine | ✅ | Motor de dominio puro + cascada + tope del 35 %, verificado (195 tests, `domain/discount` al 100 %) |
| 004 checkout-backend | ✅ | `quote` público + `orders` autenticado, cupones en BD, stock con `@Version` + reintento, migraciones V5/V6, verificado (290 tests, 99.8 %/99.4 %) |
| 005 checkout-frontend | ✅ | Cupón, desglose, banner del tope, compra (401/409) y órdenes; `CheckoutStore` con descarte de respuestas obsoletas; verificado (145 tests front, **100 %**) y commit |
| 006 docs-governance | ✅ | Arquitectura, gobernanza de IA, README y guion de demo cerrados en este lote de cambios |

**Verificación de la spec 005 (2026-10-15)**
- Frontend: `npm run test:coverage` → **145 tests, 100 %** en stmts/branch/funcs/lines; `npm run lint` y `npm run build` OK.
- Backend **sin cambios** (spec 005 es solo frontend): `./mvnw verify` → **290 tests**, fallos 0; JaCoCo LINE **99.79 %** (971/973), BRANCH **99.44 %** (179/180), idéntico a la spec 004.
- **Prueba manual con el backend en marcha** (H2 + seed): carrito `TECHNOLOGY 59.90 ×2 + HOME 34.50`. `WELCOME2026` → 3 pasos (`CATEGORY`, `VOLUME`, `COUPON`), `effectiveDiscountPercent` 25.52 %, `capApplied: false` (sin banner). `MEGA40` → 4 pasos con `CAP_ADJUSTMENT` (percent `null`, importe negativo), efectivo **35.00 %**, `capApplied: true` (banner visible). Flujo autenticado: `POST /api/checkout/orders` crea la orden, `GET /api/orders` la lista (`totalElements 1`, mismo `orderId`) y el stock baja 25 → 23.

**Verificación de la spec 004 (2026-10-15)**
- Backend: `./mvnw verify` → BUILD SUCCESS. **290 tests**; JaCoCo LINE **99.8 %** (971/973), BRANCH **99.4 %** (179/180). Los 3 huecos son heredados de la spec 001 (`SecureRandomRefreshTokenProvider.catch` inalcanzable y una rama de `RefreshService`).
- Endpoints: `POST /api/checkout/quote` (público), `POST /api/checkout/orders` (201, `USER`/`ADMIN`), `GET /api/orders` (paginado) y `GET /api/orders/{id}` (dueño o `ADMIN`; ajeno ⇒ 404).
- Sin cambios en el frontend (spec 004 es solo backend).
- De paso se arregló un test flaky heredado (`JwtIssuerTest`, corrección 10 de `ia.md`).

**Verificación de la spec 003 (2026-10-15)**
- Backend: `./mvnw verify` → BUILD SUCCESS. **195 tests**; JaCoCo LINE **99.6 %** (563/565), BRANCH **99.1 %** (113/114). Paquete `com.ecommerce.checkout.domain.discount` al **100 %** de líneas y ramas (0 de 62 ramas sin cubrir).
- La única rama sin cubrir es heredada de la spec 001 (`SecureRandomRefreshTokenProvider`, `catch (NoSuchAlgorithmException)`, inalcanzable en la práctica).
- Sin cambios en el frontend (spec 003 es solo backend).


**Verificación de la spec 002 (2026-10-15)**
- Backend: `./mvnw verify` → BUILD SUCCESS. **104 tests**; JaCoCo LINE **99.5 %** (406/408), BRANCH **98.1 %** (51/52). Módulo `catalog` al 100 %.
- Frontend: `npm run test:coverage` → **90 tests, 100 %** (stmts/branch/funcs/lines); `npm run lint` y `npm run build` OK.
- Endpoints: `GET /api/products` y `GET /api/products/{id}` (públicos); precio como string decimal exacto.

**Verificación de la spec 001 (2026-10-15)**
- Backend: `./mvnw verify` → BUILD SUCCESS. **82 tests**; JaCoCo LINE **99.4 %** (332/334), BRANCH **97.5 %** (39/40).
- Frontend: `npm run test:coverage` → **46 tests, 100 %** (stmts/branch/funcs/lines); `npm run lint` y `npm run build` OK.
- Endpoints: `POST /api/auth/{register,login,refresh,logout}` y `GET /api/auth/me`.

**Verificación del esqueleto (2026-10-15)**
- Backend: `./mvnw verify` → BUILD SUCCESS (JDK 25 local, Boot 4.1.1; JaCoCo sin clases contables).
- Frontend: `npm run lint`, `npm run test:coverage` (1 test, 100 %) y `npm run build` OK.
- CI (`.github/workflows/ci.yml`) **no ejecutado todavía** (no hay remoto).

## 2. Decisiones cerradas (no re-discutir)
Ver [docs/decisiones.md](docs/decisiones.md) (DEC-001..029). Resumen: tope del 35 % configurable y alcanzable con `MEGA40` · cupones en BD solo validación · H2 + Flyway · `@Version` para stock · sin WebSocket · backend única fuente de cálculo · HALF_UP por paso · sin `packages/` · CSS propio · auth completo con roles · `quote` público y `orders` autenticado · paquete `com.ecommerce` · refresh opaco rotado con hash y `noRollbackFor` · Signal Forms + access token solo en memoria · CSRF deshabilitado con justificación · precio como string decimal exacto (back) y céntimos enteros (front) · categoría por nombre de enum y etiqueta ES solo en UI · carrito en memoria con signals e instantánea del producto · **config del motor tipada con el enum `Category` y puente de infraestructura (dominio sin Spring)** · **tope como paso negativo `CAP_ADJUSTMENT`; solo `Money` redondea** · **etiquetas del desglose en ES desde el motor (el dato de categoría sigue siendo el enum)** · **reintento por stock envolviendo la transacción completa (`CheckoutTransaction` + `CheckoutService` sin `@Transactional`)** · **paginación de dominio con `PageResult<T>` (los puertos no exponen Spring Data)** · **la orden persiste el desglose completo y se lee sin N+1 (`@BatchSize`)** · **cupón normalizado con vigencia inclusiva y `Clock`; conflicto de stock comparte 409** · **checkout del front con store explícito y descarte de respuestas obsoletas por secuencia (no `resource`)** · **409 distinguido con un flag `stockConflict` para refrescar el catálogo** · **`returnUrl` saneado tras el login (solo rutas internas)**.

## 3. Trampas conocidas
- **Test flaky de token manipulado (spec 001, arreglado en la 004)**: cambiar el **último** carácter base64url de la firma no siempre la invalida: 32 bytes = 43 chars y el último solo codifica 2 bits, así que `A`/`Q`/`g`/`w` decodifican a la misma firma. Manipular el **payload** (segundo segmento) y no la firma. Ya corregido en `JwtIssuerTest` y `AuthFlowIntegrationTest`.
- **Signals en templates/tests**: `subtotalAmount` es un `computed`; hay que **invocarlo** (`subtotalAmount()`). Olvidar los paréntesis da "Received has type: function".
- **`querySelector` con nombres de producto**: `Monitor 27"` contiene `"` y rompe un selector por atributo (`button[aria-label="...27""]`). Buscar el botón recorriendo `querySelectorAll('button')` y comparando `getAttribute('aria-label')`.
- **`resource` + reintento en tests**: tras `reload()` el loader no corre hasta el siguiente ciclo; hace falta `fixture.detectChanges()` antes de `httpMock.expectOne(...)`.
- **Hallazgo**: con las reglas del enunciado el descuento máximo es 27.325 % → el tope no se alcanza con `WELCOME2026` (DEC-001).
- **Boot 4.1 renombra los paquetes de test**: `@WebMvcTest`/`@AutoConfigureMockMvc` están en `org.springframework.boot.webmvc.test.autoconfigure`, `@DataJpaTest` en `org.springframework.boot.data.jpa.test.autoconfigure`, `@MockitoBean` en `org.springframework.test.context.bean.override.mockito`. Verificar en los jars de `~/.m2` antes de importar de memoria.
- **Auth**: `@PreAuthorize` lanza `AccessDeniedException` dentro del controlador → el 403 hay que mapearlo en el `GlobalExceptionHandler`, no solo en el filter chain. Y el refresh necesita `noRollbackFor` para no deshacer la revocación de familia (DEC-014).
- **Refresh token en tests**: `JwtIssuerTest` usa un `Clock` fijo basado en "ahora" (truncado a segundos) porque el decoder valida la expiración contra el reloj real.
- Spring Security 7 añade la authority `FACTOR_BEARER` junto a las `ROLE_*`: **no** asertar el conjunto exacto de authorities en los tests.
- Angular 22 exige TypeScript `>=6.0 <6.1`: **no** instalar TS 7.x.
- **eslint `no-invalid-void-type`**: `void` no vale como argumento de tipo en una llamada (`post<void>`); en posición de retorno sí. Usar `post<undefined>` si hace falta.
- Jest, no Vitest (Angular usa Vitest por defecto); `jest.config.js` en CommonJS. `providers` zoneless globales vía `setupZonelessTestEnv`; no añadir `provideZonelessChangeDetection()` en los TestBed.
- **Navegación en tests de interceptores**: `expireSession()` navega a `/login`; espiar `Router.navigate` para no romper el spec.
- Los symlinks de `.claude/skills/` requieren Modo desarrollador en Windows; usa `scripts/sync-agent-links.ps1`.
- Migraciones aplicadas/planificadas: V1 auth · V2 seed users · V3 catalog · V4 seed products · V5 checkout · V6 seed coupons. No editar una aplicada.
- **`application.yml` de test sombrea el de main** (mismo nombre de recurso): al añadir `app.discounts.*` hay que ponerlo en **los dos** (`src/main/resources` y `src/test/resources`). Si falta, `DiscountProperties` lanza `IllegalStateException` en su constructor compacto y **todo `@SpringBootTest` falla al arrancar**.
- **Porcentaje efectivo ≠ importe del descuento**: el efectivo es `ratioAsPercent(totalDiscount, subtotal)` (p. ej. `5.01 / 100.10 → 5.00 %`, no 5.01). No confundir el importe redondeado con la tasa.
- **Surefire `-Dtest`** necesita patrones por **nombre de clase** (`-Dtest=MoneyTest,DiscountPipelineTest`), no globs de paquete (`com.ecommerce.checkout.domain.discount.*` → "No tests matching pattern").
- **JaCoCo excluye `com/ecommerce/**/config/**`**: `DiscountConfig`/`DiscountSettings` (cableado) no necesitan tests.
- **Los records de dominio validan con `Objects.requireNonNull`** → los nulos lanzan **`NullPointerException`**, no `IllegalArgumentException`. En los tests distinguir `assertThatNullPointerException()` de `assertThatIllegalArgumentException()` (nulos vs vacío/negativo/rango).
- **Índices del desglose**: con cupón el orden es `CATEGORY`, `VOLUME`, `COUPON` y `CAP_ADJUSTMENT` (4 pasos); el ajuste por tope es el **último** índice, no el 3 fijo.
- **`@Valid` en el genérico de una lista de un record**: Hibernate Validator avisa que es deprecado (`HV000271`); aplicarlo al **tipo** del contenedor (`List<@Valid ItemRequest> items`).
- **Un solo `@Transactional` por compra y el reintento fuera**: el reintento por `OptimisticLockingFailureException` debe reejecutar la transacción completa; si el `@Transactional` y el bucle están en el mismo método, la transacción queda rollback-only y el reintento falla. El `saveAndFlush` fuerza el `UPDATE ... WHERE version = ?` dentro de la transacción.
- **Concurrencia sobre H2**: los tests de concurrencia **no** pueden ser `@Transactional` (necesitan commits reales entre hilos); para no romper las aserciones del seed de otras suites, usar productos propios (ids 901+) insertados con `MERGE INTO products (...) KEY(id)` y borrarlos en `@AfterEach`.
- **`output<void>()` (y `post<void>`) no compila con el linter**: `no-invalid-void-type` prohíbe `void` como argumento de tipo en una llamada. Usar `output()` sin genérico (por defecto `void`) o `post<undefined>`. Igual con `ReadonlyArray<T>`, que `array-type` prohíbe: usar `readonly T[]`.
- **`TestBed.configureTestingModule` no se puede llamar dos veces** una vez instanciado el módulo: no reutilices el mismo helper `create()` dos veces en un `it`; un `it` por caso o `TestBed.resetTestingModule()` entre medias.
- **Mocks reasignados en el helper de creación**: en specs del front con un `let auth` que el helper reasigna, poner el estado (`setAccessToken`, etc.) **después** de crear el fixture; hacerlo antes lo aplica al store del test anterior y el componente ve una sesión distinta.
- **`resource.value()`**: devuelve `undefined` en `idle`/`loading` y **lanza** en `error`; no lo leas para banderas (`canNext`, etc.) fuera del bloque `@if (orders.value(); as result)`. Calcula esas banderas desde el `result` ya estrechado (p. ej. `[disabled]="result.page >= result.totalPages - 1"`).
- **`effect` de un servicio sin componente**: se ejecuta con `TestBed.tick()` (no hay `fixture.whenStable()`). El `CheckoutStore` dispara su `quote` al hacer `tick()` tras cambiar el carrito.
- **Descartar respuestas obsoletas**: `HttpClient` **no** cancela la petición anterior; el `CheckoutStore` usa un contador de secuencia (`take(1)` + comparar el id) y gana la última cotización.
- **Añadir al carrito ahora dispara `POST /api/checkout/quote`** (spec 005): en `catalog-page.spec` hay que vaciar esas peticiones (`httpMock.match('/api/checkout/quote')` y `flush`) antes del `verify()`.
- **`CurrencyPipe` acepta un `string`**: se pueden pintar los importes `string` del backend directamente (`{{ total | currency: 'USD' }}`) sin convertirlos a número.
- No uses `rm` con globs relativos en la shell de Claude (el sandbox lo bloquea); usa rutas absolutas.

- **Verificación de la spec 006 (2026-10-15)**
- Backend: `./mvnw verify` → **293 tests**, 0 fallos; JaCoCo LINE **99.79 %** (971/973), BRANCH **100.00 %** (180/180). El hueco restante es el `catch (NoSuchAlgorithmException)` de `SecureRandomRefreshTokenProvider.hash` (defensivo, inalcanzable). Se añadió un test para cubrir la rama del guard de refresh (token vacío/en blanco).
- Frontend: `npm run test:coverage` → **143 tests, 100 %** (stmts/branch/funcs/lines); `npm run lint` y `npm run build` OK.
- Auditoría final: 0 críticos/importantes, 4 menores (16–19). Documentación escrita: `docs/arquitectura.md`, `docs/ia.md`, `README.md`, `docs/demo.md`.

## 5. Bitácora de sesiones
| Fecha | Qué se hizo |
|---|---|
| 2026-10-15 | Entrevista (15 preguntas), verificación de versiones, propuesta aprobada, scaffold completo (spec 000): skills, agentes, docs, specs 001–006, esqueletos verificados |
| 2026-10-15 | Spec 001 completa: backend auth (JWT Nimbus, refresh rotado con detección de reutilización, roles) + frontend (store, interceptor con refresh único, guards, Signal Forms, cabecera); 82 tests backend (99.4 %/97.5 %) y 46 frontend (100 %); docs de cierre y `ia.md` actualizados |
| 2026-10-15 | Spec 002 completa: catálogo público (`GET /api/products`, precio string, `@Version`) + carrito reactivo (signals, céntimos enteros, tope de stock) y página con estados loading/error/empty; 104 tests backend (99.5 %/98.1 %) y 90 frontend (100 %); DEC-017..019 y `ia.md` actualizados |
| 2026-10-15 | Spec 003 completa: motor de descuentos acumulativos de **dominio puro** (`checkout/domain/discount`: Strategy + Pipeline + Factory, cascada, tope del 35 % como paso negativo `CAP_ADJUSTMENT`, `Money` único con `HALF_UP`) + puente de config `DiscountSettings`/`DiscountConfig`; 195 tests backend (LINE 99.6 %/BRANCH 99.1 %, paquete de dominio al **100 %**); DEC-020..022, `arquitectura.md` e `ia.md` (prompt 4, %, correcciones 6-7) actualizados |
| 2026-10-15 | Spec 004 completa: checkout backend — `POST /api/checkout/quote` (público) y `POST /api/checkout/orders` (autenticado) con cálculo compartido, cupones en BD (`V5`/`V6`), stock con `@Version` + `ProductRepository.save` (`saveAndFlush`) y reintento acotado (`CheckoutTransaction` + `CheckoutService`), órdenes persistidas con desglose completo y consulta por rol/propiedad (`GET /api/orders`, `/{id}`); árbol de errores `ProblemDetail` con `code`; **290 tests** (LINE 99.8 %/BRANCH 99.4 %, incluye concurrencia por la última unidad y rollback multi-línea); DEC-023..026, `security.md`, `ia.md` (prompt 5, %, correcciones 8-10) y este `MEMORY.md` actualizados |
## 5. Bitácora de sesiones
| Fecha | Qué se hizo |
|---|---|
| 2026-10-15 | Entrevista (15 preguntas), verificación de versiones, propuesta aprobada, scaffold completo (spec 000): skills, agentes, docs, specs 001–006, esqueletos verificados |
| 2026-10-15 | Spec 001 completa: backend auth (JWT Nimbus, refresh rotado con detección de reutilización, roles) + frontend (store, interceptor con refresh único, guards, Signal Forms, cabecera); 82 tests backend (99.4 %/97.5 %) y 46 frontend (100 %); docs de cierre y `ia.md` actualizados |
| 2026-10-15 | Spec 002 completa: catálogo público (`GET /api/products`, precio string, `@Version`) + carrito reactivo (signals, céntimos enteros, tope de stock) y página con estados loading/error/empty; 104 tests backend (99.5 %/98.1 %) y 90 frontend (100 %); DEC-017..019 y `ia.md` actualizados |
| 2026-10-15 | Spec 003 completa: motor de descuentos acumulativos de **dominio puro** (`checkout/domain/discount`: Strategy + Pipeline + Factory, cascada, tope del 35 % como paso negativo `CAP_ADJUSTMENT`, `Money` único con `HALF_UP`) + puente de config `DiscountSettings`/`DiscountConfig`; 195 tests backend (LINE 99.6 %/BRANCH 99.1 %, paquete de dominio al **100 %**); DEC-020..022, `arquitectura.md` e `ia.md` (prompt 4, %, correcciones 6-7) actualizados |
| 2026-10-15 | Spec 004 completa: checkout backend — `POST /api/checkout/quote` (público) y `POST /api/checkout/orders` (autenticado) con cálculo compartido, cupones en BD (`V5`/`V6`), stock con `@Version` + `ProductRepository.save` (`saveAndFlush`) y reintento acotado (`CheckoutTransaction` + `CheckoutService`), órdenes persistidas con desglose completo y consulta por rol/propiedad (`GET /api/orders`, `/{id}`); árbol de errores `ProblemDetail` con `code`; **290 tests** (LINE 99.8 %/BRANCH 99.4 %, incluye concurrencia por la última unidad y rollback multi-línea); DEC-023..026, `security.md`, `ia.md` (prompt 5, %, correcciones 8-10) y este `MEMORY.md` actualizados |
| 2026-10-15 | Spec 005 completa: checkout del frontend — `CheckoutStore` (re-cotización con descarte de respuestas obsoletas), `CheckoutApiService`, mapper `ProblemDetail`→ES, componentes `CheckoutPanel`/`CouponForm` (Signal Forms)/`BreakdownTable`/`CapAlert` (texto exacto, `role="status"`)/`OrderConfirmation`, integración en el catálogo, y `features/orders` (`OrdersPage` paginada + ruta `/orders` con `authGuard`); **145 tests front (100 %)** y backend intacto (290 tests, 99.8 %/99.4 %); DEC-027..029, `security.md`, `ia.md` (prompt 6, %, correcciones 11–15) y este `MEMORY.md` actualizados |
| 2026-10-15 | Spec 006 completa: auditoría final (0 críticos/importantes, 4 menores), documentación final (`arquitectura.md`, `ia.md`, `README.md`, `demo.md`) y cierre de gobernanza; backend **293 tests**, BRANCH **100.00 %**, LINE **99.79 %**; frontend **143 tests, 100 %**; DEC-030–031, `tasks.md` marcadas y commit pendiente |
