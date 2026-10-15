---
name: discount-test-matrix
description: Genera la matriz de tests del motor de descuentos y de checkout (casos felices y de borde exigidos por el enunciado). Úsalo al escribir o revisar tests del motor, quote o checkout.
---

# discount-test-matrix

Automatiza la creación de tests del motor a partir de una **tabla de casos**; evita tests copiados a mano y asegura los bordes del enunciado.

## Pasos
1. Lee `skills/discount-engine` y `docs/requirements.md`.
2. Construye la tabla `caso | carrito | cupón | desc. categoría | desc. volumen | desc. cupón | total desc. | % efectivo | total a pagar | capApplied`. Calcula los valores esperados **a mano** (no con el código bajo prueba).
3. Genera un `@ParameterizedTest` con `@MethodSource` (o `@CsvSource`) sobre `DiscountPipeline`, más tests aparte para errores.
4. Para el tope: construye un pipeline con `DiscountProperties` más agresivo o el cupón `MEGA40`; con las reglas por defecto el máximo posible es 27.325 % y el tope no se alcanza (ver DEC-001).
5. Corre `./mvnw verify`; si un esperado no coincide, **investiga antes de tocar el test**: puede ser un bug real.

## Casos obligatorios
| Grupo | Casos |
|---|---|
| Reglas | sin descuentos; solo categoría; volumen justo en 100.00 (no aplica) y 100.01 (aplica); cupón solo; las tres en cascada |
| Cascada | resultado ≠ suma de porcentajes; el orden importa |
| Tope | descuento = 35 % exacto (no truncar); supera 35 % → truncado y `capApplied`; el ajuste cuadra el desglose |
| Redondeo | HALF_UP en .005; desglose suma exacto |
| Entrada inválida | carrito vacío; cantidad 0 o negativa; precio negativo; producto inexistente; datos corruptos (nulls) |
| Cupón | inexistente; vencido (un día después de `valid_to`); vigente el último día; mayúsculas/espacios |
| Stock | cantidad > stock; stock exacto; stock 0; dos compras concurrentes de la última unidad |

## Anti-patrones
Esperados calculados con el mismo código bajo prueba, un solo test gigante, redondear el esperado "para que pase".
