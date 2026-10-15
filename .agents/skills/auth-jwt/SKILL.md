---
name: auth-jwt
description: Autenticación JWT con access y refresh token, roles USER/ADMIN y protección de endpoints en Spring Security 7 y en el SPA Angular. Úsalo al tocar login, registro, refresh, guards, interceptors o autorización.
---

# auth-jwt

## Backend
- Spring Security con `oauth2-resource-server` (Nimbus `JwtEncoder`/`JwtDecoder`). **No JJWT.** HS256 con `JWT_SECRET` (≥ 32 bytes, solo env). Falla al arrancar si el secreto es corto.
- Endpoints: `POST /api/auth/register` (rol siempre `USER`), `/login`, `/refresh`, `/logout`, `GET /api/auth/me`. Passwords con BCrypt.
- **Access token**: 15 min, claims `sub` (userId) y `roles`. Stateless.
- **Refresh token**: opaco aleatorio, guardado como **hash SHA-256** con `expiresAt`, `revokedAt`, `familyId`, `replacedBy`. Se **rota** en cada uso; reutilizar uno ya rotado revoca toda la familia (401). Cookie `HttpOnly; SameSite=Strict; Path=/api/auth` (`Secure` configurable por `COOKIE_SECURE`).
- Autorización: `@EnableMethodSecurity`. `USER`: catálogo, quote, checkout y **sus** órdenes. `ADMIN`: todas las órdenes. Verifica propiedad en el servicio (el rol no basta: IDOR).
- 401 vs 403 como `ProblemDetail`; CORS solo al origen del front; API stateless (CSRF deshabilitado, justificado en `docs/security.md`).
- Usuarios seed (contraseñas de **prueba**, documentadas en README): `admin@example.com` (ADMIN) y `user@example.com` (USER), hash BCrypt en la migración.

## Frontend
- Access token **en memoria** (signal en `AuthStore`), nunca `localStorage`/`sessionStorage`.
- `authInterceptor`: añade `Bearer`; ante 401 hace **un único** refresh (single-flight) y reintenta; si falla, logout y redirección.
- `authGuard` y `roleGuard` funcionales; sesión restaurada al arrancar con `/refresh`; `withCredentials` solo hacia el API propio.
- Login/registro con formularios tipados y errores `ProblemDetail` mapeados a mensajes.

## Tests
Login ok/ko, registro con email duplicado, token expirado, refresh rota, reutilización revoca la familia, 401 sin token, 403 con rol insuficiente, usuario ve solo sus órdenes, interceptor reintenta una sola vez.

## Anti-patrones
Token en storage, refresh sin rotación, secretos en código/YAML, confiar solo en el rol, loguear tokens, JWT sin expiración.
