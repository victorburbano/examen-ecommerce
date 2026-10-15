import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { EMPTY, Observable, catchError, finalize, map, of, switchMap, tap } from 'rxjs';
import { AuthApiService } from './auth-api.service';
import { toUserMessage } from './auth-error';
import { Credentials, MeResponse } from './auth.models';

/**
 * Estado de sesión en memoria. El access token **solo** vive en un signal: nunca se escribe en
 * `localStorage`/`sessionStorage`. Al recargar, `restore()` lo recupera con `POST /refresh`
 * (la cookie HttpOnly es la única prueba de identidad que persiste).
 */
@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly api = inject(AuthApiService);
  private readonly router = inject(Router);

  private readonly accessToken = signal<string | null>(null);
  private readonly currentUser = signal<MeResponse | null>(null);
  private readonly lastError = signal<string | null>(null);
  private readonly loading = signal(false);

  readonly user = this.currentUser.asReadonly();
  readonly error = this.lastError.asReadonly();
  readonly busy = this.loading.asReadonly();
  readonly email = computed(() => this.currentUser()?.email ?? null);
  readonly roles = computed<readonly string[]>(() => this.currentUser()?.roles ?? []);
  readonly isAuthenticated = computed(() => this.accessToken() !== null);
  readonly isAdmin = computed(() => this.roles().includes('ADMIN'));

  /** Access token actual (para el interceptor). */
  token(): string | null {
    return this.accessToken();
  }

  register(email: string, password: string): Observable<boolean> {
    this.lastError.set(null);
    this.loading.set(true);
    return this.api.register({ email, password } satisfies Credentials).pipe(
      map(() => true),
      catchError((error: unknown) => {
        this.lastError.set(toUserMessage(error));
        return of(false);
      }),
      finalize(() => this.loading.set(false)),
    );
  }

  login(email: string, password: string): Observable<boolean> {
    this.lastError.set(null);
    this.loading.set(true);
    return this.api.login({ email, password } satisfies Credentials).pipe(
      tap((tokens) => this.accessToken.set(tokens.accessToken)),
      switchMap(() => this.loadUser()),
      map(() => true),
      catchError((error: unknown) => {
        this.lastError.set(toUserMessage(error));
        this.clear();
        return of(false);
      }),
      finalize(() => this.loading.set(false)),
    );
  }

  /** Restaura la sesión al arrancar. Nunca lanza: sin cookie simplemente no hay sesión. */
  restore(): Observable<void> {
    if (this.accessToken() !== null) {
      return EMPTY;
    }
    this.loading.set(true);
    return this.api.refresh().pipe(
      tap((tokens) => this.accessToken.set(tokens.accessToken)),
      switchMap(() => this.loadUser()),
      map(() => undefined),
      catchError(() => {
        this.clear();
        return EMPTY;
      }),
      finalize(() => this.loading.set(false)),
    );
  }

  logout(): Observable<void> {
    return this.api.logout().pipe(
      catchError(() => EMPTY),
      finalize(() => {
        this.clear();
        void this.router.navigate(['/login']);
      }),
    );
  }

  /** Usado por el interceptor cuando el refresh ha rotado el token. */
  setAccessToken(accessToken: string): void {
    this.accessToken.set(accessToken);
  }

  /** El refresh falló: se cierra la sesión y se vuelve al login. */
  expireSession(): void {
    this.clear();
    void this.router.navigate(['/login']);
  }

  clear(): void {
    this.accessToken.set(null);
    this.currentUser.set(null);
  }

  private loadUser(): Observable<MeResponse> {
    return this.api.me().pipe(tap((user) => this.currentUser.set(user)));
  }
}
