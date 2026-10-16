# Guion de la demo (7 minutos)

Objetivo: mostrar las 4 HU y los casos de borde del enunciado con datos reales. Todos los importes de esta página se capturaron ejecutando la app (2026-10-15) contra el backend descrito.

## Preparación (antes de empezar)
Requisitos: **JDK 25** y **Node `^22.22.3 || ^24.15.0 || ^26`**. Maven no hace falta (se usa el wrapper `./mvnw` / `.\mvnw.cmd`). `JWT_SECRET` es **obligatorio** y debe medir **≥ 32 bytes** o el backend no arranca.

**Linux / macOS / Git Bash** (desde la raíz del repo):
```bash
# Terminal 1 — backend (genera un secreto aleatorio)
cd apps/backend && JWT_SECRET="$(openssl rand -base64 48)" ./mvnw spring-boot:run
# Terminal 2 — frontend
cd apps/frontend && npm ci && npm start
```

**Windows (PowerShell)** (desde la raíz del repo):
```powershell
# Terminal 1 — backend
cd apps\backend
$env:JWT_SECRET = "dev-local-secret-cambia-esto-0123456789abcdef"   # ≥ 32 bytes
.\mvnw.cmd spring-boot:run
# Terminal 2 — frontend
cd apps\frontend
npm ci
npm start
```

### Si `JWT_SECRET` da problemas
Debe medir **≥ 32 bytes** o el backend no arranca. Comandos exactos por plataforma:

```bash
# Linux / macOS / Git Bash
export JWT_SECRET="$(openssl rand -base64 48)"
```
```powershell
# Windows (PowerShell)
$env:JWT_SECRET = [Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Max 256 }))
```
> Alternativa en cualquier SO: una cadena literal de 32+ caracteres, p. ej. `dev-local-secret-cambia-esto-0123456789abcdef`.

### URLs locales y credenciales de prueba
| Servicio | URL / conexión | Credenciales |
|---|---|---|
| **Frontend** (SPA) | `http://localhost:4200` | usuarios seed ([README](../README.md)) |
| **Backend** (API) | `http://localhost:8080` | JWT: `Authorization: Bearer <accessToken>` |
| **Salud del backend** | `http://localhost:8080/actuator/health` | público |
| **Base de datos** (H2 en memoria) | `jdbc:h2:mem:shop;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE` | usuario `sa` · contraseña *(vacía)* |
| **Consola H2** (ver la BD) | `http://localhost:8080/h2-console` | JDBC URL `jdbc:h2:mem:shop` · usuario `sa` · contraseña *(vacía)* |

Abrir `http://localhost:4200` (el proxy de `npm start` reenvía `/api` a `http://localhost:8080`). La BD es **H2 en memoria embebida** (sin servidor aparte): cada reinicio del backend recarga el seed. Usuarios y cupones en el [README](../README.md).

### Ver la base de datos desde el navegador (consola H2)
1. Con el backend en marcha, abrir **`http://localhost:8080/h2-console`**.
2. En *JDBC URL* poner `jdbc:h2:mem:shop` · *User* `sa` · *Password* vacío → **Connect**.
3. Consultas útiles para la demo:
   - `SELECT * FROM products;` — stock antes/después de una compra.
   - `SELECT code, percent, valid_from, valid_to FROM coupons;` — cupones seed.
   - `SELECT * FROM orders;` y `SELECT * FROM order_discounts;` — orden persistida con su desglose.
   > Es una utilidad **solo local/demo** (`/h2-console` queda público, ver [DEC-032](decisiones.md)); en producción se desactiva.


> Los ejemplos con `curl` de este guion usan sintaxis **bash** (incluida la continuación de línea con `\`): en Windows ejecútalos en Git Bash/WSL o usa `curl.exe`/`Invoke-RestMethod`.

## 0:00–1:00 · Catálogo (HU 1)
- La página principal lista los productos seed con **precio como string exacto** (`"59.90"`), categoría y stock.
- Señalar: el precio viaja como string y el carrito suma en **céntimos enteros** (nunca `float`); el **stock solo cambia por checkout**.
- Fijar el carrito de la demo: **2 × Auriculares Bluetooth (59.90, TECHNOLOGY)** + **1 × Lámpara de Escritorio (34.50, HOME)**.

## 1:00–2:00 · Carrito (HU 1)
- Añadir/quitar unidades; el subtotal reacciona al instante (`CartStore` con `signals`).
- **Subtotal: 154.30 USD**. El front solo suma; **no calcula descuentos** (eso es del backend).

## 2:00–3:00 · Cupón `WELCOME2026` y desglose (HU 2)
- En el panel de checkout, escribir `WELCOME2026` → **Aplicar**. El desglose lo devuelve `POST /api/checkout/quote`:

| Línea | % | Importe |
|---|---|---|
| Descuento de categoría (TECHNOLOGY) | 10.00 | 11.98 |
| Descuento por volumen (> 100.00) | 5.00 | 7.12 |
| Cupón WELCOME2026 | 15.00 | 20.28 |
| **Total descuento** | **25.52 % efectivo** | **39.38** |
| **Total a pagar** | | **114.92** |

- Sin banner: `capApplied = false` (25.52 % < 35 %).
- Enseñar que cada regla aplica en **cascada** sobre el total corriente, no sumando porcentajes.

> Comando equivalente para verificar en vivo:
> ```bash
> curl -s localhost:8080/api/checkout/quote -H 'Content-Type: application/json' \
>   -d '{"items":[{"productId":1,"quantity":2},{"productId":5,"quantity":1}],"couponCode":"WELCOME2026"}'
> ```

## 3:00–4:00 · Cupón `MEGA40`, tope del 35 % y banner (HU 4)
- Cambiar el cupón a `MEGA40` (40 %). Ahora el descuento acumulado supera el tope, así que el motor añade la línea **"Ajuste por tope de ahorro"** en negativo:

| Línea | % | Importe |
|---|---|---|
| Descuento de categoría | 10.00 | 11.98 |
| Descuento por volumen | 5.00 | 7.12 |
| Cupón MEGA40 | 40.00 | 54.08 |
| **Ajuste por tope de ahorro** | — | **−19.17** |
| **Total descuento** | **35.00 % efectivo** | **54.01** |
| **Total a pagar** | | **100.29** |

- `capApplied = true` → aparece el banner persistente con el **texto exacto**: *"¡Enhorabuena! Has alcanzado el límite máximo de ahorro permitido (35%)"* (`role="status"`).
- Clave de diseño: el tope es `round(0.35 × subtotal original)` = `round(0.35 × 154.30)` = **54.01**; la línea de ajuste hace que la **suma del desglose cuadre** con el descuento total.

## 4:00–5:00 · Caso de borde: stock insuficiente (409)
- El backend valida stock **en el momento de la compra**, no en la cotización. Con el producto 6 (Taza, stock 2):
> ```bash
> curl -s -X POST localhost:8080/api/checkout/orders -H "Authorization: Bearer <token>" \
>   -H 'Content-Type: application/json' -d '{"items":[{"productId":6,"quantity":3}]}'
> ```
> ```json
> {"status":409,"code":"INSUFFICIENT_STOCK","productId":6,"requested":3,"available":2}
> ```
- En la UI: el panel muestra el mensaje y **refresca el catálogo** (`stockConflict` → `products.reload()`). Escenario de UI: mantener una pestaña con el carrito y, en otra, comprar el mismo producto hasta agotarlo; al comprar desde la pestaña con el stock obsoleto → 409.

## 5:00–6:00 · Compra y orden persistida (HU 3)
- Iniciar sesión como `user@example.com` / `User123!` (el access token vive **solo en memoria**; el refresh en cookie `HttpOnly`).
- Comprar **2 × Cargador USB-C (19.90)**:

| Concepto | Valor |
|---|---|
| Subtotal | 39.80 |
| Descuento de categoría (10 %) | 3.98 |
| **Total** | **35.82** |
| `orderId` | UUID devuelto (201) |

- Verificar persistencia y efecto en stock:
> ```bash
> curl -s localhost:8080/api/orders -H "Authorization: Bearer <token>"   # totalElements: 1
> curl -s localhost:8080/api/products/4                                   # stock 25 -> 23
> ```
- La compra es **transaccional** (`@Version` + reintento): nunca queda stock descontado sin orden.

## 6:00–7:00 · Órdenes por rol (RF-A2)
- `user@example.com` en `/orders`: **"Mis órdenes"** (solo las suyas).
- Cerrar sesión, entrar como `admin@example.com` / `Admin123!` y abrir `/orders`: **"Todas las órdenes"**.
- El **backend** decide el alcance (el front solo cambia el título); pedir una orden ajena devuelve **404** (no revela que existe).

---

## Preguntas probables del jurado (y respuesta corta)

**1. ¿Por qué el tope no se alcanza con las reglas "de libro"?**
Con 10 % + 5 % + 15 % en cascada el máximo es `1 − 0.90 × 0.95 × 0.85 = 27.325 %`. El 35 % solo se alcanza con cupones mayores; por eso el seed incluye `MEGA40` (DEC-001) para poder demostrar el banner.

**2. ¿Cómo garantizáis que el total es exacto?**
`BigDecimal` con escala 2 y `HALF_UP` **en cada paso**; solo `Money` usa `RoundingMode`. El invariante "suma de las líneas = descuento total" se verifica en todos los casos de la matriz (incluido el paso negativo del tope).

**3. ¿Qué pasa si dos usuarios compran a la vez el último artículo?**
Lock optimista (`@Version`): uno commitea y el otro recibe `OptimisticLockingFailureException`; `CheckoutService` reintenta hasta 3 veces sobre una **transacción nueva** cada vez y, si no queda stock, responde 409. Cubierto por `CheckoutConcurrencyIntegrationTest`.

**4. ¿El cliente puede falsear el descuento?**
No. El `quote` y la compra **recalculan** desde la BD (`CheckoutCalculation`): el backend ignora cualquier precio/descuento del cliente (DEC-006). El front nunca replica el motor.

**5. ¿Por qué `quote` es público?**
Para mostrar el desglose (HU 2) antes de iniciar sesión. No tiene efectos secundarios (`@Transactional(readOnly = true)`); la compra sí exige `USER`/`ADMIN`.

**6. ¿Cómo se aísla el motor de descuentos?**
Es dominio puro en `checkout/domain/discount`: sin Spring, JPA ni HTTP. Un test de arquitectura (`DiscountDomainArchitectureTest`) falla si aparece un import de framework. Patrones: **Strategy** (`DiscountRule`), **Pipeline** (`DiscountPipeline`), **Factory** (`DiscountPipelineFactory`), **Port/Adapter** (`OrderRepository` ↔ JPA).

**7. ¿Qué pasa con el refresh token si lo roban y lo reutilizan?**
Es opaco, rotado en cada uso y guardado solo como hash SHA-256; el `markRotated` es una actualización condicional. Si se reutiliza, se **revoca la familia** entera y se responde 401 (`@Transactional(noRollbackFor=...)` para que la revocación persista aunque la respuesta sea 401).

**8. ¿Por qué Jest y no Vitest (el default de Angular 22)?**
El equipo ya usa Jest; `jest-preset-angular` 17 soporta Angular 22 zoneless. La guía de Vitest del skill oficial de Angular está anulada por `frontend-testing-jest`.

**9. ¿Cómo se gestiona el 401 y el refresh concurrente?**
`authInterceptor` marca la petición ya reintentada (`HttpContextToken`) y hace **un único** refresh compartido (`shareReplay`) para las peticiones concurrentes; si el refresh falla, cierra la sesión y va a `/login` con `returnUrl` saneado (solo rutas internas).

**10. ¿Cobertura real y cómo se fuerza el umbral?**
Backend **293 tests**, LINE 99.79 % / BRANCH 100.00 % (JaCoCo falla el build < 80 %); frontend **143 tests, 100 %** (umbral en `jest.config.js`). Único hueco: un `catch` inalcanzable (SHA-256 garantizado por el JVM).

**11. ¿Qué cambiaríais para producción?**
H2 → PostgreSQL (misma capa JPA), `COOKIE_SECURE=true` + HTTPS, rotación de `JWT_SECRET`, rate limiting de login, y stock distribuido (el lock optimista es de una sola instancia).

**12. ¿Por qué un commit por spec?**
SDD: cada spec (`spec → plan → tasks → prompt`) se cierra en un commit revisable; además permite medir el porcentaje de código asistido por IA con los trailers `Co-Authored-By` ([docs/ia.md](ia.md)).
