/**
 * Mensajes de error para el usuario a partir de la respuesta RFC 9457 del backend.
 * Sin `any`: se estrecha el tipo a mano.
 */
export function toUserMessage(error: unknown): string {
  if (!isHttpErrorResponse(error)) {
    return 'No se ha podido completar la operación';
  }
  const problem = error.error;
  if (isProblemDetail(problem)) {
    return problem.detail;
  }
  return error.status === 401
    ? 'Credenciales incorrectas'
    : 'No se ha podido completar la operación';
}

interface HttpErrorLike {
  readonly status: number;
  readonly error: unknown;
}

interface ProblemDetailLike {
  readonly detail: string;
}

function isHttpErrorResponse(value: unknown): value is HttpErrorLike {
  return (
    typeof value === 'object' &&
    value !== null &&
    'status' in value &&
    typeof (value as { status: unknown }).status === 'number' &&
    'error' in value
  );
}

function isProblemDetail(value: unknown): value is ProblemDetailLike {
  return (
    typeof value === 'object' &&
    value !== null &&
    'detail' in value &&
    typeof (value as { detail: unknown }).detail === 'string'
  );
}
