# Seguridad

Detalle de implementación en el skill `auth-jwt`. Se completa en las specs 001 y 004.

| Tema | Decisión |
|---|---|
| Autenticación | Access JWT 15 min (HS256, Nimbus, `JWT_SECRET` ≥ 32 bytes por env). Refresh opaco rotado, guardado como hash SHA-256, con detección de reutilización (revoca la familia) |
| Transporte del refresh | Cookie `HttpOnly`, `SameSite=Strict`, `Path=/api/auth`, `Secure` configurable (`COOKIE_SECURE`) |
| Access token en el SPA | Solo en memoria; nunca `localStorage` |
| Autorización | `@PreAuthorize` por rol + comprobación de propiedad en el servicio (anti-IDOR): `USER` solo ve sus órdenes, `ADMIN` todas |
| CSRF | API stateless con Bearer; la cookie de refresh está limitada a `/api/auth` y `SameSite=Strict` |
| Validación | `jakarta.validation` en DTOs; el checkout recalcula y nunca confía en totales del cliente |
| Errores | `ProblemDetail`; sin stack traces ni datos sensibles |
| Secretos | Solo `.env.example`; usuarios seed con contraseñas de **prueba** (README) |
| Pendiente (mejora) | Rate limiting / lockout de login, rotación de `JWT_SECRET`, HTTPS |

## Revisión de la spec 001 (2026-10-15)

Comprobaciones del prompt `docs/prompts/security-review.md` (hallazgo y estado):

| Comprobación | Resultado |
|---|---|
| Secretos fuera del repo | ✅ Solo `.env.example`; `JWT_SECRET` por entorno y validado (≥ 32 bytes) en el arranque (`SecurityProperties`). La migración de seed guarda **hashes** BCrypt, nunca contraseñas |
| Tokens no logueados ni en storage | ✅ No hay `localStorage`/`sessionStorage` en el SPA (el access token vive en un signal; el refresh en cookie `HttpOnly`). El backend no registra tokens: `GlobalExceptionHandler` loguea la excepción, no el cuerpo |
| Rotación y reutilización del refresh | ✅ Un solo uso (`markRotated` condicional); reutilizar o usar un token revocado **revoca la familia** y responde 401 (`RefreshService:50`, `noRollbackFor` para que la revocación persista) |
| 401 vs 403 | ✅ 401 en entry point y en credenciales/refresh inválidos; 403 en `AccessDeniedException` (`GlobalExceptionHandler:55`, `@PreAuthorize` dentro del controlador) |
| Roles y propiedad (IDOR) | ✅ `@PreAuthorize` por rol; en esta spec no hay recursos de usuario que comprobar. Hay un test de sondeo de rol |
| Validación de input | ✅ `jakarta.validation` en los DTOs (`@Email`, `@NotBlank`, `@Size`); 400 con `errors[]` |
| CORS al origen del front | ✅ `CorsConfigurationSource` acotado a `app.security.allowed-origin`, credenciales solo para `/api/**` |
| `ProblemDetail` sin stack traces | ✅ RFC 9457 con `code` estable; el 500 devuelve "Unexpected error" y no filtra detalles |

Revisión equivalente a `code-auditor` y `coverage-guard` (no disponibles fuera de Claude Code): diff revisado a mano contra SOLID, tipado estricto (sin `any`), cero lógica en controladores y umbrales de cobertura verificados.

## Revisión de la spec 004 (2026-10-15)

Comprobaciones del prompt `docs/prompts/security-review.md` sobre el checkout (hallazgo y estado):

| Comprobación | Resultado |
|---|---|
| Dinero recalculado en el servidor | ✅ `quote` y `orders` usan el **mismo** `CheckoutCalculation`; el cliente solo manda `productId` y `quantity` (los precios/totales que envíe se ignoran). Hay test de que un `total` inyectado no afecta a la respuesta |
| Autorización de `orders` | ✅ `POST /api/checkout/orders` y `GET /api/orders**` exigen `USER`/`ADMIN` (`@PreAuthorize`); `POST /api/checkout/quote` es público (DEC-012, sin efectos secundarios) |
| Propiedad (anti-IDOR) | ✅ `OrderQueryService` comprueba dueño o `ADMIN`; una orden de otro `USER` responde **404** (nunca 403), para no revelar su existencia. Test dedicado con dos `USER` y un `ADMIN` |
| Validación de input | ✅ `jakarta.validation` en `CheckoutRequest` (`@NotNull`/`@Positive`); `productId` duplicado o cuerpo sin `items` → 400. El servicio revalida con tipos de dominio (`RequestedItem`) |
| Integridad del stock | ✅ `@Version` + `decremento` con `saveAndFlush` dentro de una transacción; el `CHECK (stock >= 0)` y la regla de dominio impiden stock negativo; reintento acotado y 409 al agotarse |
| Sin datos sensibles en errores/logs | ✅ `ProblemDetail` con `code` estable; los `WARN` de reintento registran solo el número de intento, nunca el token ni el carrito |
| Cupones | ✅ Existen y vigencia (inclusiva) verificadas con `Clock` inyectado; inexistente/vencido → 422 con código distinto, sin filtrar datos de otros cupones |
| Secretos / CORS | ✅ Sin cambios: sin secretos en el repo; CORS sigue acotado a `app.security.allowed-origin` |

Revisión equivalente a `code-auditor` y `coverage-guard` (no disponibles fuera de Claude Code): diff revisado a mano contra SOLID (Strategy/Pipeline/Factory ya de la spec 003, Port/Adapter en el checkout, cero lógica en controllers), tipado estricto (sin `any` en Java, records para DTOs), dinero siempre `BigDecimal`/strings y umbrales de cobertura verificados (`./mvnw verify` en verde, LINE 99.8 % / BRANCH 99.4 %).
