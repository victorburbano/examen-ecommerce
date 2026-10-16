import { isUnauthorized, problemCode, toCheckoutMessage } from './checkout-error';

function httpError(status: number, body: unknown): unknown {
  return { status, error: body };
}

describe('checkout-error', () => {
  it('reads the stable code from the ProblemDetail body', () => {
    expect(problemCode(httpError(422, { code: 'COUPON_NOT_FOUND' }))).toBe('COUPON_NOT_FOUND');
  });

  it('reports no code when the body is not a ProblemDetail', () => {
    expect(problemCode(httpError(0, null))).toBeNull();
    expect(problemCode(httpError(500, { code: 42 }))).toBeNull();
    expect(problemCode('boom')).toBeNull();
    expect(problemCode(undefined)).toBeNull();
  });

  it('maps the known codes to UI messages', () => {
    const cases: readonly (readonly [string, string])[] = [
      ['COUPON_NOT_FOUND', 'El cupón no existe.'],
      ['COUPON_EXPIRED', 'El cupón ha caducado.'],
      ['INSUFFICIENT_STOCK', 'No hay stock suficiente para completar la compra.'],
      ['PRODUCT_NOT_FOUND', 'Uno de los productos ya no está disponible.'],
      ['EMPTY_CART', 'El carrito está vacío.'],
      ['INVALID_CART', 'El carrito o el cupón no son válidos.'],
      ['VALIDATION_ERROR', 'Alguno de los datos enviados no es válido.'],
    ];

    for (const [code, message] of cases) {
      expect(toCheckoutMessage(httpError(422, { code }), 'fallback')).toBe(message);
    }
  });

  it('falls back to the detail when the code is unknown', () => {
    expect(toCheckoutMessage(httpError(500, { code: 'SOMETHING', detail: 'Boom' }), 'fallback')).toBe(
      'Boom',
    );
  });

  it('ignores an empty detail and uses the fallback', () => {
    expect(toCheckoutMessage(httpError(500, { detail: '   ' }), 'fallback')).toBe('fallback');
  });

  it('uses the fallback for a plain network error', () => {
    expect(toCheckoutMessage(httpError(0, null), 'no se pudo')).toBe('no se pudo');
    expect(toCheckoutMessage(new Error('down'), 'no se pudo')).toBe('no se pudo');
  });

  it('detects an unauthorized error', () => {
    expect(isUnauthorized(httpError(401, { code: 'UNAUTHORIZED' }))).toBe(true);
    expect(isUnauthorized(httpError(409, { code: 'INSUFFICIENT_STOCK' }))).toBe(false);
    expect(isUnauthorized(null)).toBe(false);
  });
});
