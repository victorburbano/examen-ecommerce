import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

/**
 * Rutas de la app. El catálogo (spec 002) es la página principal; auth (spec 001) aporta login y
 * registro en diferido; las órdenes (spec 005) requieren sesión. `authGuard`/`roleGuard` se aplican
 * a las rutas que lo requieran.
 */
export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () => import('./features/catalog/catalog-page/catalog-page').then((module) => module.CatalogPage),
    title: 'Catálogo',
  },
  {
    path: 'orders',
    canActivate: [authGuard],
    loadComponent: () => import('./features/orders/orders-page/orders-page').then((module) => module.OrdersPage),
    title: 'Órdenes',
  },
  {
    path: '',
    loadChildren: () => import('./features/auth/auth.routes').then((module) => module.AUTH_ROUTES),
  },
  { path: '**', redirectTo: '' },
];
