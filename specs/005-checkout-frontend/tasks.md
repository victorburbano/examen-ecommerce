# Tasks 005

- [ ] Modelos TS y `CheckoutApiService`; mapper de errores `ProblemDetail`
- [ ] `CheckoutStore` (re-cotización, descarte de respuestas obsoletas, `capApplied`)
- [ ] `CheckoutPanel`, `CouponForm`, `BreakdownTable`, `CapAlert`, `OrderConfirmation`
- [ ] Compra: sesión requerida (redirige a login y regresa), doble clic, 409 con refresco de stock
- [ ] `OrdersPage` con paginación y alcance por rol
- [ ] Responsivo y accesibilidad (`role="status"`, `role="alert"`)
- [ ] Tests Jest: store, componentes, banner (texto exacto), cupón (éxito/errores), compra (éxito/401/409), órdenes por rol
- [ ] `npm run lint`, `test:coverage` ≥ 80 %, `build`; prueba manual con `WELCOME2026` y `MEGA40`
- [ ] `code-auditor`, `coverage-guard`; `docs/decisiones.md`, `ia-log`, `MEMORY.md`
- [ ] Commit `feat(checkout-ui): coupon, breakdown, cap alert and order flow (spec 005)`
