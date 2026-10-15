# Spec 005 — Checkout frontend (HU 2 y HU 4)

## Objetivo
Permitir aplicar un cupón, ver el desglose calculado por el backend, recibir la alerta persistente al llegar al tope del 35 %, confirmar la compra y consultar órdenes.

## Flujo
1. El carrito (spec 002) muestra el subtotal local. Con ítems, el panel de checkout pide `POST /api/checkout/quote` al cambiar el carrito y al pulsar "Aplicar" cupón.
2. Se muestra el desglose: subtotal original, líneas de descuento (Categoría, Volumen, Cupón y, si aplica, ajuste por tope), **% de descuento efectivo**, **ahorro total** y **total a pagar**.
3. Si `capApplied` es true: banner persistente y distintivo con el texto exacto *"¡Enhorabuena! Has alcanzado el límite máximo de ahorro permitido (35%)"*.
4. "Comprar" llama a `POST /api/checkout/orders` (si no hay sesión, redirige a `/login` y vuelve al carrito conservando los ítems); al éxito muestra confirmación con `orderId` y totales, vacía el carrito y refresca el catálogo (stock).
5. `/orders`: `USER` ve sus órdenes; `ADMIN` ve todas (tabla paginada simple).

## Criterios de aceptación
- El cálculo **no se replica** en el front: todos los montos del desglose vienen del backend (DEC-006).
- Aplicar `WELCOME2026` con el carrito del ejemplo muestra los tres descuentos, 24.22 %, ahorro 31.49 y total 98.51 (con precios seed equivalentes).
- Cupón inexistente o vencido: mensaje de error claro bajo el campo, el carrito y el desglose previo se conservan.
- Aplicar `MEGA40` sobre un carrito grande muestra el banner del tope; el banner **sigue visible** mientras `capApplied` sea true y desaparece si el carrito deja de alcanzarlo. Con `WELCOME2026` no aparece.
- Estados visibles: calculando, error de red, carrito vacío (sin checkout), compra en curso (botón deshabilitado), stock insuficiente (409) con mensaje y refresco del catálogo.
- Accesibilidad: el banner usa `role="status"`/`aria-live`, el error del cupón `role="alert"`, foco visible.
- Responsivo (móvil y escritorio), CSS propio.
- Cobertura ≥ 80 % del front con tests de: `CheckoutStore`, panel de desglose, banner (visible/oculto/texto exacto), cupón (éxito/errores), compra (éxito, 401 → login, 409), `OrdersPage` por rol.

## Contrato consumido
El de la spec 004 (`quote`, `orders`, `GET /api/orders`). Modelos TS `readonly` que reflejan los DTOs; montos como `string` y formateados con `CurrencyPipe` (USD).

## Casos de borde
Cambiar el carrito con cupón aplicado (se re-cotiza y se mantiene el cupón), quitar el último ítem (se limpia el desglose y el banner), doble clic en "Comprar" (una sola orden), respuesta lenta que llega tarde (se ignora la obsoleta: gana la última cotización).

## Fuera de alcance
WebSocket/SSE (DEC-005), pasarela de pago real, edición de cupones.
