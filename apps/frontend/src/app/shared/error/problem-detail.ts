/**
 * Lectura del formato de error RFC 9457 (`ProblemDetail`) que emite el backend. Único punto de
 * estrechado (sin `any`) compartido por los mappers de auth (spec 001) y checkout (spec 005).
 */

/** Forma mínima de un error HTTP de Angular (`status` + `error`), sin acoplarse a `HttpErrorResponse`. */
export interface HttpErrorLike {
  readonly status: number;
  readonly error: unknown;
}

function asRecord(value: unknown): Record<string, unknown> | null {
  return typeof value === 'object' && value !== null ? (value as Record<string, unknown>) : null;
}

/** ¿Tiene la forma de un error HTTP (`status` numérico + `error`)? */
export function isHttpErrorLike(value: unknown): value is HttpErrorLike {
  const record = asRecord(value);
  return record !== null && typeof record['status'] === 'number' && 'error' in record;
}

/** Código estable del `ProblemDetail` (p. ej. `COUPON_EXPIRED`), o `null` si no lo trae. */
export function problemCode(error: unknown): string | null {
  const body = asRecord(asRecord(error)?.['error']);
  const code = body?.['code'];
  return typeof code === 'string' ? code : null;
}

/** Campo `detail` del `ProblemDetail` (no vacío), o `null`. */
export function problemDetail(error: unknown): string | null {
  const body = asRecord(asRecord(error)?.['error']);
  const detail = body?.['detail'];
  return typeof detail === 'string' && detail.trim() !== '' ? detail : null;
}

/** ¿El error es un 401 (sesión ausente o caducada)? */
export function isUnauthorized(error: unknown): boolean {
  return asRecord(error)?.['status'] === 401;
}
