# Requisitos — Core E-Commerce con Descuentos Acumulativos

Resumen del enunciado de la prueba técnica (20 min de sustentación: 7 demo · 7 arquitectura · 6 auditoría de IA).

## Reglas de negocio (orden de precedencia, en cascada)
1. **Categoría**: si hay productos "Tecnología", 10 % sobre el precio de **esos** productos.
2. **Volumen**: si el subtotal tras la regla 1 es **> $100 USD**, 5 % sobre todo el carrito.
3. **Cupón**: cupón vigente y registrado (ej. `WELCOME2026` = 15 %) sobre el total tras la regla 2.
4. **Tope**: el descuento total nunca supera el **35 %** del subtotal original; si lo supera se trunca exactamente al 35 %.

Decisiones de interpretación: ver [decisiones.md](decisiones.md) (DEC-001 tope, DEC-002 cupones en BD, DEC-007 redondeo).

## Historias de usuario del enunciado
| HU | Resumen | Spec |
|---|---|---|
| HU 1 | Ver productos (id, nombre, precio, categoría, stock) y agregar/quitar del carrito con subtotal en tiempo real | 002 |
| HU 2 | Aplicar cupón y ver desglose (categoría, volumen, cupón), % efectivo, ahorro total y total a pagar | 005 |
| HU 3 | Checkout: validar stock, recalcular, decrementar stock, persistir orden y devolver totales exactos | 004 |
| HU 4 | Notificación persistente "¡Enhorabuena! Has alcanzado el límite máximo de ahorro permitido (35%)" | 005 |

## Requisitos añadidos por el equipo
| ID | Requisito | Spec |
|---|---|---|
| RF-A1 | Register, login, refresh (rotado) y logout con access + refresh token | 001 |
| RF-A2 | Roles `USER` y `ADMIN`; `ADMIN` ve todas las órdenes, `USER` solo las suyas | 001, 004 |
| RF-A3 | Usuarios seed de prueba (`admin@example.com`, `user@example.com`) | 001 |
| RF-C1 | Cupones en BD (`code`, `percent`, `valid_from`, `valid_to`); solo **validar** (sin CRUD ni UI de administración) | 004 |
| RF-C2 | Cupones seed: `WELCOME2026` 15 %, `EXPIRED2025` vencido, `MEGA40` 40 % | 004 |
| RF-D1 | Porcentajes, umbral y tope configurables (`app.discounts.*`) | 003 |

## No funcionales
- Backend Java 25 + Spring Boot 4.1.1; frontend Angular 22 (standalone, signals, zoneless); tipado estricto sin `any`.
- Persistencia H2 en memoria con seed (el enunciado admite memoria, SQLite o JSON).
- Cobertura ≥ 80 % en front y back; casos de borde: tope exacto, carrito vacío o corrupto, cupón inexistente o vencido, stock insuficiente.
- Al menos **2 patrones** implementados y justificados en `docs/arquitectura.md`.
- Monorepo con commits descriptivos e incrementales (uno por spec).
- Entregables: `docs/arquitectura.md`, `docs/ia.md`, README con instalación, variables de entorno y comandos.

## Hallazgo clave
Con las reglas dadas, el máximo posible es `1 − 0.90 × 0.95 × 0.85 = 27.325 %` (carrito 100 % Tecnología + cupón de 15 %). El tope del 35 % **no se alcanza** salvo con cupones mayores (`MEGA40`) o porcentajes configurados más altos. Ver DEC-001.
