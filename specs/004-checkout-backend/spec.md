# Spec 004 — Checkout backend (HU 3)

## Objetivo
Cotizar y procesar una orden de forma consistente: validar cupón y stock, recalcular con el motor (spec 003), decrementar stock, persistir la orden y devolver los totales desglosados exactos.

## Datos seed
`V6__seed_coupons.sql`: `WELCOME2026` 15 % (2026-01-01 → 2026-12-31) · `EXPIRED2025` 10 % (2025-01-01 → 2025-12-31) · `MEGA40` 40 % (2026-01-01 → 2026-12-31, para demostrar el tope).
Esquema `V5__checkout_schema.sql`: `coupons`, `orders` (id UUID, `user_id`, `created_at`, `original_subtotal`, `total_discount`, `total`, `effective_discount_percent`, `cap_applied`, `coupon_code` nullable), `order_items` (`order_id`, `product_id`, `name`, `category`, `unit_price`, `quantity`, `line_total`) y `order_discounts` (`order_id`, `type`, `label`, `percent`, `amount`).

## Contrato
Cuerpo común: `{ "items": [{ "productId": 1, "quantity": 2 }], "couponCode": "WELCOME2026" }` (cupón opcional). El cliente **nunca** envía precios ni totales: se leen de la BD.

| Endpoint | Acceso | Respuesta |
|---|---|---|
| `POST /api/checkout/quote` | público (sin efectos secundarios; ver DEC-012) | 200 `Breakdown` |
| `POST /api/checkout/orders` | `USER` o `ADMIN` autenticado | 201 `{orderId, createdAt, ...Breakdown}` |
| `GET /api/orders` | `USER` sus órdenes · `ADMIN` todas | 200 paginado (`page`, `size`) |
| `GET /api/orders/{id}` | dueño o `ADMIN` | 200 · 404 si es de otro `USER` |

`Breakdown`: `{items:[{productId,name,category,unitPrice,quantity,lineTotal}], originalSubtotal, discounts:[{type,label,percent,amount}], totalDiscount, effectiveDiscountPercent, total, capApplied, couponCode}` con montos como string decimal.

Errores (`ProblemDetail` + `code`): 400 `EMPTY_CART` / `INVALID_CART` (cantidad ≤ 0, `productId` repetido, campos nulos) · 404 `PRODUCT_NOT_FOUND` · 409 `INSUFFICIENT_STOCK` · 422 `COUPON_NOT_FOUND` / `COUPON_EXPIRED` · 401 / 403.

## Reglas
- `quote` y `orders` usan el mismo caso de cálculo; `orders` además valida stock, decrementa y persiste **en una sola transacción** (todo o nada).
- Stock: `Product.decreaseStock` + `@Version`; ante `OptimisticLockException`, reintentar (máx. 3) releyendo el stock; si ya no alcanza ⇒ 409.
- Cupón: código normalizado (mayúsculas, sin espacios); vigencia inclusiva en `valid_from` y `valid_to` con `Clock` inyectado.
- `quote` **no** valida stock (se valida solo al comprar); sí rechaza productos inexistentes y cupones inválidos.

## Criterios de aceptación
- Quote del caso "Tecnología 80 + Hogar 50 + WELCOME2026" devuelve los pasos y totales de la spec 003 (con precios seed equivalentes).
- `MEGA40` con carrito grande ⇒ `capApplied = true`, `effectiveDiscountPercent = 35.00`, paso `CAP_ADJUSTMENT` presente.
- Order creada: stock decrementado, orden y desgloses persistidos, respuesta idéntica al quote previo.
- Dos compras simultáneas de la última unidad: **exactamente una** orden creada, la otra 409, stock final 0.
- Orden fallida (stock insuficiente en la 2ª línea) ⇒ no se decrementa el stock de la 1ª (rollback).
- Cupón inexistente ⇒ 422 `COUPON_NOT_FOUND`; vencido ⇒ 422 `COUPON_EXPIRED`; carrito vacío ⇒ 400.
- `USER` solo ve sus órdenes; `ADMIN` ve todas; otro `USER` pidiendo `/orders/{id}` ajeno ⇒ 404.
- Sin token en `orders` ⇒ 401; los totales enviados por el cliente (si los manda) se ignoran.
- Cobertura ≥ 80 % (global del backend).

## Casos de borde
Cantidad igual al stock, stock 0, producto inexistente, `productId` duplicado, cupón en minúsculas con espacios, cupón el último día de vigencia y un día después, `couponCode` vacío (se trata como sin cupón), body corrupto o sin `items`.

## Fuera de alcance
CRUD de cupones, pagos reales, cancelación/devolución de órdenes, frontend (spec 005).
