import { Routes } from '@angular/router';

/** Rutas lazy del feature de auth (spec 001). */
export const AUTH_ROUTES: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./login-page/login-page').then((module) => module.LoginPage),
    title: 'Entrar',
  },
  {
    path: 'register',
    loadComponent: () => import('./register-page/register-page').then((module) => module.RegisterPage),
    title: 'Crear cuenta',
  },
  { path: '', pathMatch: 'full', redirectTo: 'login' },
];
