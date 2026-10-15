# Tasks 001

## Backend
- [x] Migraciones `V1__auth_schema`, `V2__seed_users` y `application.yml` (H2, `app.security.*`)
- [x] Dominio y puertos (`User`, `Role`, `RefreshToken`, repos, excepciones)
- [x] `JwtIssuer`, `BcryptPasswordHasher`, generador de refresh, `SecurityProperties` (valida secreto ≥ 32 bytes)
- [x] Servicios `Register`, `Login`, `Refresh` (rotación + reutilización), `Logout`
- [x] `SecurityConfig`, converter de roles, 401/403 como `ProblemDetail`
- [x] `AuthController`, DTOs, mapper, cookie factory, `GlobalExceptionHandler`
- [x] Tests unit + `@WebMvcTest` + integración (flujo y reutilización); `./mvnw verify` ≥ 80 % → **82 tests, LINE 99.4 %, BRANCH 97.5 %**

## Frontend
- [x] `AuthStore`, `AuthApiService`, `authInterceptor`, guards
- [x] `LoginPage`, `RegisterPage`, cabecera con sesión, rutas lazy, proxy `/api`
- [x] Tests Jest; `npm run lint`, `test:coverage`, `build` en verde → **46 tests, 100 % (stmts/branch/funcs/lines)**

## Cierre
- [x] `security-review`, `code-auditor`, `coverage-guard` (equivalente manual: no hay estos sub-agentes fuera de Claude Code)
- [x] README (usuarios seed y variables), `docs/security.md`, `docs/decisiones.md`, `ia-log`, `MEMORY.md`
- [x] Commit `feat(auth): register, login, refresh rotation and roles (spec 001)`
