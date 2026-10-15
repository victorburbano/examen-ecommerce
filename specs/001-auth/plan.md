# Plan 001

Skills: `auth-jwt`, `spring-backend-conventions`, `backend-testing`, `frontend-angular-conventions`, `frontend-testing-jest`, `angular-developer`. Context7: Spring Security 7 `oauth2-resource-server`/Nimbus, Spring Data JPA, Flyway, Angular 22 (interceptors funcionales, guards, Signal Forms).

## Backend (`com.ecommerce.auth` + `shared`)
- `domain`: `User`, `Role` (enum), `RefreshToken`, puertos `UserRepository`, `RefreshTokenRepository`, `PasswordHasher`, `TokenGenerator`; excepciones `InvalidCredentialsException`, `EmailAlreadyTakenException`, `InvalidRefreshTokenException`.
- `application`: `RegisterService`, `LoginService`, `RefreshService` (rotación + reutilización), `LogoutService`; un caso de uso por clase.
- `infrastructure`: entidades/repos JPA, `BcryptPasswordHasher`, `JwtIssuer` (Nimbus `JwtEncoder`), `SecureRandomTokenGenerator`, `SecurityProperties` (`app.security.*`).
- `api`: `AuthController`, DTOs `record`, `AuthMapper`, `RefreshCookieFactory`.
- `shared/security`: `SecurityConfig` (resource server, `@EnableMethodSecurity`, stateless, CORS), `JwtAuthenticationConverter` (claim `roles` → `ROLE_*`). `shared/error`: `GlobalExceptionHandler` (`ProblemDetail` + `code`), 401/403 entry point/handler.
- Migraciones: `V1__auth_schema.sql` (`users`, `refresh_tokens` con índices por `family_id`, `user_id`), `V2__seed_users.sql` (hashes BCrypt generados al implementar).
- `application.yml`: H2 en memoria, `ddl-auto=validate`, Flyway, `app.security.*` con env vars.
- Tests: unit (servicios con Mockito, `Clock` fijo), `@WebMvcTest` (validación, 401/403, cookies), integración H2 (flujo login → refresh → reuso).

## Frontend
- `core/auth`: `AuthStore`, `AuthApiService`, `authInterceptor` (single-flight), `authGuard`, `roleGuard`, modelos `readonly`.
- `features/auth`: `LoginPage`, `RegisterPage` (Signal Forms), rutas lazy; cabecera con sesión.
- `app.config.ts`: `withInterceptors([authInterceptor])`, inicializador que restaura sesión.
- Proxy de desarrollo `/api` → `localhost:8080` (`proxy.conf.json`).
- Tests Jest: store, interceptor (401 → un refresh), guards, formularios.
