import { Routes } from '@angular/router';

/**
 * Rutas de la app. El catálogo (spec 002) es la página principal; auth (spec 001) aporta login y
 * registro en diferido. `authGuard`/`roleGuard` se aplican a las rutas que lo requieran.
 */
export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () => import('./features/catalog/catalog-page/catalog-page').then((module) => module.CatalogPage),
    title: 'Catálogo',
  },
  {
    path: '',
    loadChildren: () => import('./features/auth/auth.routes').then((module) => module.AUTH_ROUTES),
  },
  { path: '**', redirectTo: '' },
];
