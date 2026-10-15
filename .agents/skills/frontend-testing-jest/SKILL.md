---
name: frontend-testing-jest
description: Tests del frontend Angular 22 con Jest 30 y jest-preset-angular 17 (zoneless), cobertura >= 80 %. Úsalo al escribir o revisar tests TypeScript. Anula las guías de Vitest del skill angular-developer.
---

# frontend-testing-jest

**Solo Jest.** Sin Jasmine, Karma ni Vitest. Lo que `angular-developer` diga de Vitest se adapta a Jest (`jest.fn()`, `jest.spyOn`, `jest.useFakeTimers()`).

## Setup (ya creado)
- `jest.config.js` (CommonJS) con `preset: 'jest-preset-angular'`, `setupFilesAfterEnv: ['<rootDir>/setup-jest.ts']`, `coverageThreshold.global` ≥ 80. Solo se excluyen specs, `app.config.ts` y `*.routes.ts`.
- `setup-jest.ts`: `setupZonelessTestEnv()`.
- Comando: `npm run test:coverage`; informe en `apps/frontend/coverage/`.

## Reglas
- `TestBed` + `provideHttpClient()` + `provideHttpClientTesting()` + `HttpTestingController` para servicios e interceptors.
- Signals: afirma sobre `store.subtotal()` tras `store.add(...)`; con zoneless usa `await fixture.whenStable()`.
- Componentes: pruebas por comportamiento (DOM), no por detalles internos.
- Interceptor de auth: Bearer añadido; 401 → un solo refresh y un reintento; refresh fallido → logout.
- Guards con `TestBed.runInInjectionContext`. Timers con `jest.useFakeTimers()`.
- Un `*.spec.ts` junto a cada unidad.

## Casos obligatorios
- **Carrito**: agregar, quitar, cantidades, subtotal en tiempo real, carrito vacío, no superar el stock.
- **Alerta del tope**: se muestra con `capApplied: true` y el texto exacto; no se muestra con `false`; persiste mientras aplique.
- **Cupón**: aplicar con éxito (desglose, % efectivo, ahorro, total) y errores (inexistente/vencido) mostrados.
- **Auth**: interceptor/refresh, guards por rol, formularios con validación.

## Anti-patrones
`any` en mocks, `setTimeout` reales, tests acoplados al orden, excluir archivos del coverage para llegar al 80 %.
