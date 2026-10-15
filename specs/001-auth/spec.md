# Spec 001 — Auth (backend + frontend)

## Objetivo
Registro, login, refresh rotado y logout con access + refresh token; roles `USER` y `ADMIN`; usuarios seed. Base de seguridad para que el checkout y las órdenes queden ligados al usuario.

## Reglas
- Access JWT 15 min (HS256, claims `sub` = userId, `roles`). Refresh opaco, 7 días, rotado en cada uso, guardado como hash SHA-256.
- Reutilizar un refresh ya rotado ⇒ se revoca toda su familia y responde 401.
- Registro siempre crea rol `USER`. Email único (normalizado a minúsculas). Password ≥ 8 caracteres, BCrypt.
- Usuarios seed (migración): `admin@example.com` (ADMIN) y `user@example.com` (USER), contraseñas de prueba documentadas en README.

## Contrato (backend)
| Endpoint | Acceso | Request → Response |
|---|---|---|
| `POST /api/auth/register` | público | `{email,password}` → 201 `{id,email,role}` · 409 `EMAIL_TAKEN` · 400 validación |
| `POST /api/auth/login` | público | `{email,password}` → 200 `{accessToken,tokenType:"Bearer",expiresInSeconds}` + cookie `refresh_token` · 401 credenciales |
| `POST /api/auth/refresh` | cookie | → 200 igual que login + cookie rotada · 401 inválido/expirado/reutilizado |
| `POST /api/auth/logout` | cookie | → 204 (idempotente, revoca la familia) |
| `GET /api/auth/me` | autenticado | → 200 `{userId,email,roles}` |

Cookie: `HttpOnly; SameSite=Strict; Path=/api/auth; Secure` según `COOKIE_SECURE`. Errores como `ProblemDetail` con `code`.

## Criterios de aceptación
- Login correcto devuelve access token y cookie; password incorrecta o email inexistente ⇒ 401 con el mismo mensaje (sin enumerar usuarios).
- Refresh válido emite tokens nuevos e invalida el anterior; reusar el anterior ⇒ 401 y la familia queda revocada (el token nuevo también falla).
- Endpoint protegido sin token ⇒ 401; con rol insuficiente ⇒ 403 (ambos `ProblemDetail`).
- Registro con email duplicado ⇒ 409; password corta ⇒ 400 con `errors[]`.
- `JWT_SECRET` < 32 bytes ⇒ la app no arranca.
- Ningún token ni password aparece en logs.
- **Frontend**: pantallas `/login` y `/register` (formularios tipados con validación); `AuthStore` con signals guarda el access token **en memoria**; `authInterceptor` añade Bearer y ante 401 hace **un único refresh** y reintenta, y si falla cierra sesión; `authGuard` y `roleGuard`; la sesión se restaura al arrancar con `/refresh`; la cabecera muestra el email y "Salir".
- Cobertura ≥ 80 % en el código nuevo (back y front).

## Casos de borde
Email con mayúsculas/espacios, refresh sin cookie, refresh expirado, dos refresh simultáneos del mismo token (uno gana, el otro dispara reutilización), logout sin sesión, token manipulado.

## Fuera de alcance
Verificación de email, recuperación de contraseña, rate limiting (se documenta como mejora), UI de gestión de usuarios.
