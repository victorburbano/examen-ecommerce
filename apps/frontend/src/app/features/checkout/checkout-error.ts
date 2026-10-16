import { problemCode, problemDetail } from '../../shared/error/problem-detail';

/**
 * Único punto de traducción de errores del checkout a mensajes de UI. Usa el `code` estable de
 * `ProblemDetail` (RFC 9457) que emite el backend y cae al `detail` si el código es desconocido.
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
