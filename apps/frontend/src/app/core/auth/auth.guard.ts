import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from './auth.store';

/** Solo deja pasar con sesión iniciada; si no, redirige al login. */
export const authGuard: CanActivateFn = () => {
  const store = inject(AuthStore);
  return store.isAuthenticated() ? true : inject(Router).createUrlTree(['/login']);
};

/** Exige un rol concreto (p. ej. `ADMIN`). Sin sesión → login; con sesión pero sin rol → home. */
export function roleGuard(role: string): CanActivateFn {
  return () => {
    const store = inject(AuthStore);
    const router = inject(Router);
    if (!store.isAuthenticated()) {
      return router.createUrlTree(['/login']);
    }
    return store.roles().includes(role) ? true : router.createUrlTree(['/']);
  };
}
