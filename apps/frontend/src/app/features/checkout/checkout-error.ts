/**
 * Único punto de traducción de errores del checkout a mensajes de UI. Usa el `code` estable de
 * `ProblemDetail` (RFC 9457) que emite el backend y cae al `detail` si el código es desconocido.
 * Sin `any`: se estrecha el tipo a mano.
 */

/** Mensajes por código estable del `ProblemDetail`. */
const CODE_MESSAGES: Readonly<Record<string, string>> = {
  COUPON_NOT_FOUND: 'El cupón no existe.',
  COUPON_EXPIRED: 'El cupón ha caducado.',
  INSUFFICIENT_STOCK: 'No hay stock suficiente para completar la compra.',
  PRODUCT_NOT_FOUND: 'Uno de los productos ya no está disponible.',
  EMPTY_CART: 'El carrito está vacío.',
  INVALID_CART: 'El carrito o el cupón no son válidos.',
  VALIDATION_ERROR: 'Alguno de los datos enviados no es válido.',
};

function asRecord(value: unknown): Record<string, unknown> | null {
  return typeof value === 'object' && value !== null ? (value as Record<string, unknown>) : null;
}

/** Código estable del `ProblemDetail`, o `null` si la respuesta no lo trae. */
export function problemCode(error: unknown): string | null {
  const body = asRecord(asRecord(error)?.['error']);
  const code = body?.['code'];
  return typeof code === 'string' ? code : null;
}

/** Mensaje del `ProblemDetail` (campo `detail`), o `null` si no existe. */
function problemDetail(error: unknown): string | null {
  const body = asRecord(asRecord(error)?.['error']);
  const detail = body?.['detail'];
  return typeof detail === 'string' && detail.trim() !== '' ? detail : null;
}

/**
 * Mensaje para el usuario. `fallback` se usa cuando el error no sigue el formato
 * `ProblemDetail` (p. ej. un fallo de red con `status` 0).
 */
export function toCheckoutMessage(error: unknown, fallback: string): string {
  const code = problemCode(error);
  if (code !== null && CODE_MESSAGES[code] !== undefined) {
    return CODE_MESSAGES[code];
  }
  return problemDetail(error) ?? fallback;
}

/** ¿El error es un 401 (sesión ausente o caducada)? */
export function isUnauthorized(error: unknown): boolean {
  return asRecord(error)?.['status'] === 401;
}
