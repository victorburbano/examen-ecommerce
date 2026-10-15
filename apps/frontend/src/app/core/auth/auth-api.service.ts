import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Credentials, MeResponse, RegisterResponse, TokenResponse } from './auth.models';

/**
 * Llamadas HTTP de autenticación. No guarda estado: de eso se encarga `AuthStore`.
 * Solo se envían credenciales (cookie de refresh) hacia nuestro propio API.
 */
@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/auth';

  register(payload: Credentials): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.baseUrl}/register`, payload);
  }

  login(payload: Credentials): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.baseUrl}/login`, payload, { withCredentials: true });
  }

  refresh(): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.baseUrl}/refresh`, null, { withCredentials: true });
  }

  /**
   * Logout (204 No Content). Se usa `post<undefined>` porque la regla
   * `no-invalid-void-type` prohíbe `void` como argumento de tipo en una llamada.
   */
  logout(): Observable<void> {
    return this.http.post<undefined>(`${this.baseUrl}/logout`, null, { withCredentials: true });
  }

  me(): Observable<MeResponse> {
    return this.http.get<MeResponse>(`${this.baseUrl}/me`);
  }
}
