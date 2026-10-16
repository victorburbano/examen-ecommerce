import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from './auth.store';

/** Solo deja pasar con sesión iniciada; si no, redirige al login. */
export const authGuard: CanActivateFn = () => {
  const store = inject(AuthStore);
  return store.isAuthenticated() ? true : inject(Router).createUrlTree(['/login']);
};
