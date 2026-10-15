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
