/** DTOs del backend (spec 001). Todos de solo lectura: el estado vive en signals. */

/** Credenciales de login/registro. Modelo de los formularios con Signal Forms. */
export interface Credentials {
  readonly email: string;
  readonly password: string;
}

/** Cuerpo de login y refresh. El refresh token viaja en una cookie HttpOnly, no aquí. */
export interface TokenResponse {
  readonly accessToken: string;
  readonly tokenType: string;
  readonly expiresInSeconds: number;
}

/** Usuario recién registrado. */
export interface RegisterResponse {
  readonly id: number;
  readonly email: string;
  readonly role: string;
}

/** Usuario autenticado (GET /me). */
export interface MeResponse {
  readonly userId: number;
  readonly email: string;
  readonly roles: readonly string[];
}
