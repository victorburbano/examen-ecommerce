# Spec 002 — Catálogo y carrito (HU 1)

## Objetivo
Listar productos pre-configurados con su stock y permitir agregarlos y quitarlos de un carrito en tiempo real; el subtotal original se actualiza al instante.

## Datos seed (migración `V4__seed_products.sql`)
| id | Nombre | Categoría | Precio USD | Stock |
|---|---|---|---|---|
| 1 | Auriculares Bluetooth | Tecnología | 59.90 | 10 |
| 2 | Teclado Mecánico | Tecnología | 89.00 | 8 |
| 3 | Monitor 27" | Tecnología | 249.00 | 3 |
| 4 | Cargador USB-C | Tecnología | 19.90 | 25 |
| 5 | Lámpara de Escritorio | Hogar | 34.50 | 12 |
| 6 | Taza de Cerámica | Hogar | 9.99 | 2 |
| 7 | Camiseta de Algodón | Ropa | 24.00 | 20 |
| 8 | Zapatillas Running | Ropa | 79.00 | 0 |

Hay productos con stock bajo (6) y sin stock (8) para la demo.

## Contrato (backend)
`GET /api/products` · público · 200 `[{id, name, price:"59.90", category, stock}]` (precio como string decimal).
`GET /api/products/{id}` · público · 200 o 404 `PRODUCT_NOT_FOUND`.
Sin endpoints de escritura (el stock solo cambia por checkout, spec 004).

## Reglas de dominio
`Product` (entidad): `@Version`, `decreaseStock(qty)` lanza `InsufficientStockException` si `qty > stock` o `qty ≤ 0`; `hasStockFor(qty)`. (El decremento se usa en la spec 004; aquí queda definido y probado.)

## Criterios de aceptación
- `GET /api/products` devuelve los 8 productos con precios exactos (`BigDecimal`, scale 2).
- **Frontend**: la página principal lista productos en tarjetas con nombre, categoría, precio y stock; el botón "Agregar" se deshabilita sin stock (muestra "Sin stock") o al alcanzar el stock disponible.
- El carrito (`CartStore` con signals) permite agregar, quitar una unidad, eliminar la línea y vaciar; **subtotal e ítems se actualizan de inmediato** (`computed`), sin pasar por el backend.
- Carrito vacío: mensaje y totales en 0.00. Estados de carga y error del catálogo visibles.
- Diseño responsivo (móvil y escritorio), CSS propio.
- El carrito vive en memoria (sobrevive a la navegación y al login, no al recargar).
- Cobertura ≥ 80 % en el código nuevo.

## Casos de borde
Agregar más unidades que el stock (ignorado/deshabilitado), quitar de un carrito vacío, producto que desaparece del catálogo, precios con 2 decimales (suma exacta sin errores de `float`: el subtotal local se calcula en céntimos enteros).

## Fuera de alcance
CRUD de productos, imágenes, búsqueda, persistencia del carrito.
