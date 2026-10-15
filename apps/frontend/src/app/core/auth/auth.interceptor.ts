import {
  HttpContextToken,
  HttpErrorResponse,
  HttpInterceptorFn,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, catchError, finalize, shareReplay, switchMap, throwError } from 'rxjs';
import { AuthApiService } from './auth-api.service';
import { AuthStore } from './auth.store';
import { TokenResponse } from './auth.models';

/** Marca la petición ya reintentada para no entrar en un bucle de refresh. */
const RETRIED = new HttpContextToken<boolean>(() => false);

/** Endpoints que no llevan Bearer y cuyo 401 nunca debe disparar un refresh. */
const PUBLIC_AUTH_PATHS = ['/api/auth/register', '/api/auth/login', '/api/auth/refresh'];

let refreshInFlight: Observable<TokenResponse> | null = null;

function isPublicAuthRequest(url: string): boolean {
  return PUBLIC_AUTH_PATHS.some((path) => url.includes(path));
}

/**
 * Añade el access token a las peticiones privadas. Ante un 401 hace **un único** refresh
 * (compartido entre las peticiones concurrentes) y reintenta la original una sola vez; si el
 * refresh falla, cierra la sesión.
 */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const store = inject(AuthStore);
  const api = inject(AuthApiService);

  const token = store.token();
  const authorized =
    token !== null && !isPublicAuthRequest(request.url)
      ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : request;

  return next(authorized).pipe(
    catchError((error: unknown) => {
      if (
        !(error instanceof HttpErrorResponse) ||
        error.status !== 401 ||
        isPublicAuthRequest(request.url) ||
        request.context.get(RETRIED)
      ) {
        return throwError(() => error);
      }

      if (refreshInFlight === null) {
        refreshInFlight = api.refresh().pipe(
          catchError((refreshError: unknown) => {
            store.expireSession();
            return throwError(() => refreshError);
          }),
          finalize(() => {
            refreshInFlight = null;
          }),
          shareReplay({ bufferSize: 1, refCount: false }),
        );
      }

      return refreshInFlight.pipe(
        switchMap((tokens) => {
          store.setAccessToken(tokens.accessToken);
          const retried = request.clone({
            setHeaders: { Authorization: `Bearer ${tokens.accessToken}` },
            context: request.context.set(RETRIED, true),
          });
          return next(retried);
        }),
      );
    }),
  );
};
