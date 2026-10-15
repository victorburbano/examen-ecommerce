---
name: frontend-angular-conventions
description: Convenciones del frontend Angular 22 de examen-ecommerce - standalone, signals, tipado estricto, rutas lazy, servicios, interceptors y store del carrito. Úsalo al crear o modificar código del frontend.
---

# frontend-angular-conventions

Complementa al skill externo `angular-developer`. Si difieren, **este skill manda** (sobre todo en testing: Jest, no Vitest).

## Estructura
`src/app/core` (auth, interceptors, guards, config, api) · `features/{auth,catalog,cart,checkout}` (rutas, componentes, store, `*ApiService`, modelos) · `shared` (UI reutilizable, pipes).

## Reglas
- **Standalone** siempre, `OnPush`, app **zoneless**, `inject()`.
- **Signals**: `signal` para estado, `computed` para derivados (subtotal, cantidad), `linkedSignal`/`resource`/`httpResource` para datos async. `effect` solo para efectos reales.
- **Observer = signals**: `CartStore` expone `items`, `subtotal`, `itemCount` como `computed`; los componentes reaccionan sin suscripciones manuales.
- **Stores por feature** (`@Injectable` con signals privados y API pública `readonly`). Los componentes no llaman a `HttpClient`; usan un `*ApiService` y el store.
- **El backend calcula descuentos.** El front solo suma el subtotal local; el desglose, el % efectivo y `capApplied` vienen de `POST /api/checkout/quote`. Prohibido replicar la lógica de descuentos en TypeScript.
- **Alerta del tope (HU 4)**: `quote().capApplied` → banner persistente y distintivo (`role="status"`) con *"¡Enhorabuena! Has alcanzado el límite máximo de ahorro permitido (35%)"*; sigue visible mientras `capApplied` sea true.
- **Tipado estricto**: `strict`, sin `any`, modelos `readonly`, DTOs tipados que reflejan el contrato de la spec; `unknown` + type guards para datos externos.
- **Dinero**: montos como `string` decimal del API; muestra con `CurrencyPipe` (USD). No acumules con `number` como fuente de verdad.
- **Formularios**: Signal Forms con validación tipada (login, registro, cupón).
- **Rutas**: lazy por feature, `authGuard`/`roleGuard` funcionales.
- **HTTP**: `provideHttpClient(withInterceptors([...]))`; `ProblemDetail` mapeado a mensajes de UI en un único lugar.
- **Estilos**: CSS propio, responsivo (Grid/Flex, variables CSS, mobile-first). Sin librería de UI.
- **Accesibilidad**: etiquetas, foco visible, `aria-live` para alertas y estados de carga.
- Naming: `angular-developer/references/naming-conventions.md`.

## Checklist
- [ ] OnPush + signals, sin `any`, sin lógica de negocio en templates.
- [ ] Un servicio API, un store, componentes simples.
- [ ] Estados de carga, vacío y error en cada vista.
- [ ] Tests Jest (`frontend-testing-jest`).

## Anti-patrones
`subscribe()` anidados, estado mutable suelto, `any`, `ngOnInit` que replica un `computed`, token en storage, calcular descuentos en el cliente.
