# Tasks 001

## Backend
- [ ] Migraciones `V1__auth_schema`, `V2__seed_users` y `application.yml` (H2, `app.security.*`)
- [ ] Dominio y puertos (`User`, `Role`, `RefreshToken`, repos, excepciones)
- [ ] `JwtIssuer`, `BcryptPasswordHasher`, generador de refresh, `SecurityProperties` (valida secreto ≥ 32 bytes)
- [ ] Servicios `Register`, `Login`, `Refresh` (rotación + reutilización), `Logout`
- [ ] `SecurityConfig`, converter de roles, 401/403 como `ProblemDetail`
- [ ] `AuthController`, DTOs, mapper, cookie factory, `GlobalExceptionHandler`
- [ ] Tests unit + `@WebMvcTest` + integración (flujo y reutilización); `./mvnw verify` ≥ 80 %

## Frontend
- [ ] `AuthStore`, `AuthApiService`, `authInterceptor`, guards
- [ ] `LoginPage`, `RegisterPage`, cabecera con sesión, rutas lazy, proxy `/api`
- [ ] Tests Jest; `npm run lint`, `test:coverage`, `build` en verde

## Cierre
- [ ] `security-review`, `code-auditor`, `coverage-guard`
- [ ] README (usuarios seed y variables), `docs/security.md`, `docs/decisiones.md`, `ia-log`, `MEMORY.md`
- [ ] Commit `feat(auth): register, login, refresh rotation and roles (spec 001)`
