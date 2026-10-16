import { isHttpErrorLike, isUnauthorized, problemDetail } from '../../shared/error/problem-detail';

/**
 * Mensajes de error para el usuario a partir de la respuesta RFC 9457 del backend.
 * El estrechado vive en `shared/error/problem-detail` (compartido con el checkout).
 */
const GENERIC_MESSAGE = 'No se ha podido completar la operación';

export function toUserMessage(error: unknown): string {
  if (!isHttpErrorLike(error)) {
    return GENERIC_MESSAGE;
  }
  const detail = problemDetail(error);
  if (detail !== null) {
    return detail;
  }
  return isUnauthorized(error) ? 'Credenciales incorrectas' : GENERIC_MESSAGE;
}
