# Registro de decisiones

Formato: `DEC-NNN` · fecha · estado · contexto · decisión · alternativas · consecuencias. Las decisiones no se borran: se marcan *reemplazada por DEC-XXX*. Alimenta la sección de trade-offs de `arquitectura.md`.

## DEC-001 — El tope del 35 % es configurable y alcanzable vía cupones
- **Fecha / estado**: 2026-10-15 · aceptada.
- **Contexto**: con 10 % + 5 % + 15 % en cascada el máximo es 27.325 % (suma simple: 30 %). El tope del 35 % y la HU 4 no se podrían demostrar con datos reales.
- **Decisión**: implementar las reglas exactamente como el enunciado; porcentajes, umbral y tope en `app.discounts.*`; el cupón trae su % desde la BD. El cupón seed `MEGA40` (40 %) activa el tope en la demo. El tope se prueba además con pipelines de reglas más agresivas.
- **Alternativas**: consultar al evaluador (sin canal); hardcodear un caso (frágil); bajar el tope (viola el enunciado).
- **Consecuencias**: la demo muestra el banner con `MEGA40`; se explica abiertamente que con `WELCOME2026` el máximo es 27.3 %.

## DEC-002 — Cupones en BD, solo validación
- **Estado**: aceptada.
- **Decisión**: tabla `coupons(code, percent, valid_from, valid_to)` con seed por migración. Sin CRUD ni UI. Validación (existe y vigente, con `Clock` inyectable) en el backend al cotizar y al comprar; el front solo envía el código.
- **Alternativas**: cupón hardcodeado `WELCOME2026` (no permite probar "no registrado/expirado"); CRUD completo (fuera de alcance).
- **Consecuencias**: errores de dominio `CouponNotFoundException` / `CouponExpiredException` → `ProblemDetail`.

## DEC-003 — H2 en memoria con JPA + Flyway
- **Estado**: aceptada.
- **Decisión**: H2 en memoria; esquema y seed con migraciones Flyway; repositorios detrás de puertos.
- **Alternativas**: SQLite (dialecto de comunidad en Hibernate), JSON (sin transacciones ni concurrencia), PostgreSQL (exige Docker para correr y probar).
- **Consecuencias**: cero instalación; los datos se reinician con la app (ventaja para la demo); cambiar de motor solo toca `infrastructure` y la config.

## DEC-004 — Stock con optimistic locking
- **Estado**: aceptada.
- **Decisión**: `@Version` en `Product`; la regla "no bajar de 0" vive en la entidad; reintento acotado ante `OptimisticLockException`; test de concurrencia con la última unidad.
- **Alternativas**: pesimista (`SELECT FOR UPDATE`; exige ordenar bloqueos para evitar deadlocks); `UPDATE ... WHERE stock >= n` (atómico y simple, pero saca la regla de dominio al repositorio).
- **Consecuencias**: sin bloqueos largos; el perdedor recibe 409 "sin stock" tras releer.

## DEC-005 — Sin WebSocket
- **Estado**: aceptada (YAGNI).
- **Contexto**: la alerta de la HU 4 responde a una acción del propio usuario.
- **Decisión**: `quote` devuelve `capApplied`; el front muestra el banner. Sin WebSocket/SSE.
- **Consecuencias**: menos complejidad; el stock en vivo entre usuarios queda como mejora futura.

## DEC-006 — El backend es la única fuente de cálculo
- **Estado**: aceptada.
- **Decisión**: el front solo suma el subtotal local; el desglose sale de `POST /api/checkout/quote`; `checkout` recalcula siempre.
- **Consecuencias**: sin lógica duplicada ni divergencias; cada quote cuesta una llamada de red.

## DEC-007 — Redondeo HALF_UP por paso
- **Estado**: aceptada.
- **Decisión**: `BigDecimal` scale 2, `RoundingMode.HALF_UP`; cada descuento se redondea en su paso para que el desglose sume exacto. Tope = `round(0.35 × subtotal original)`.
- **Alternativas**: HALF_EVEN (bancario; difiere de una calculadora común); redondear solo al final (el desglose podría no sumar).

## DEC-008 — Sin `packages/` compartido
- **Estado**: aceptada.
- **Decisión**: el back es Java, no hay código TS que compartir. El contrato se documenta en cada spec y el front tipa a mano los DTOs; generar tipos desde OpenAPI queda como mejora.

## DEC-009 — CSS propio, sin librería de UI
- **Estado**: aceptada.
- **Decisión**: CSS propio responsivo (Grid/Flex, variables CSS). El enunciado no exige librería; menos dependencias que defender.

## DEC-010 — Auth completo con roles
- **Estado**: aceptada.
- **Decisión**: register, login, refresh rotado con detección de reutilización, logout; roles `USER` y `ADMIN` (ADMIN ve todas las órdenes); usuarios seed. No lo pide el enunciado: se asume el costo de tiempo a favor de seguridad y órdenes ligadas al usuario.
- **Riesgo**: consume tiempo de una prueba corta; se mitiga reutilizando el diseño de `wallet-bank` y limitando el alcance a lo de la spec 001.

## DEC-012 — `quote` público, `checkout` autenticado
- **Estado**: aceptada (refinamiento de la respuesta "el checkout exige login").
- **Decisión**: `POST /api/checkout/quote` es público porque no tiene efectos secundarios (permite ver el desglose y aplicar el cupón sin obligar a loguearse antes); `POST /api/checkout/orders` exige autenticación y liga la orden al usuario.
- **Alternativa**: quote autenticado (fuerza login antes de aplicar el cupón; peor UX para la demo).
- **Riesgo**: el endpoint es consultable sin sesión; no expone datos de usuarios ni modifica estado. Rate limiting queda como mejora.

## DEC-013 — Paquete raíz `com.ecommerce`
- **Estado**: aceptada.
- **Decisión**: módulos `com.ecommerce.{auth,catalog,checkout,shared}`; se evita `com.ecommerce.checkout.checkout`.

## DEC-011 — SDD con formato propio y un commit por spec
- **Estado**: aceptada.
- **Decisión**: `specs/NNN-nombre/{spec,plan,tasks,prompt}.md`; un commit por spec con trailer `Co-Authored-By`. Se descarta GitHub Spec Kit (tercero, más pesado de defender).
