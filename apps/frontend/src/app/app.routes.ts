import { Routes } from '@angular/router';

/**
 * Rutas de la app. Cada feature se carga en diferido; auth (spec 001) aporta login y registro.
 * Los guards `authGuard`/`roleGuard` se aplican en las specs que protegen rutas de catálogo/checkout.
 */
export const routes: Routes = [
  {
    path: '',
    loadChildren: () => import('./features/auth/auth.routes').then((module) => module.AUTH_ROUTES),
  },
  { path: '**', redirectTo: 'login' },
];
