import { HttpErrorResponse } from '@angular/common/http';
import { toUserMessage } from './auth-error';

describe('toUserMessage', () => {
  it('uses the ProblemDetail detail when present', () => {
    const error = new HttpErrorResponse({
      status: 401,
      error: { detail: 'Invalid email or password' },
    });

    expect(toUserMessage(error)).toBe('Invalid email or password');
  });

  it('falls back to a generic message for a 401 without ProblemDetail', () => {
    expect(toUserMessage(new HttpErrorResponse({ status: 401, error: null }))).toBe(
      'Credenciales incorrectas',
    );
  });

  it('falls back to a generic message for other statuses', () => {
    expect(toUserMessage(new HttpErrorResponse({ status: 500, error: 'boom' }))).toBe(
      'No se ha podido completar la operación',
    );
  });

  it('ignores values that are not an HttpErrorResponse', () => {
    expect(toUserMessage('nope')).toBe('No se ha podido completar la operación');
    expect(toUserMessage(null)).toBe('No se ha podido completar la operación');
    expect(toUserMessage({ detail: 'x' })).toBe('No se ha podido completar la operación');
  });
});
