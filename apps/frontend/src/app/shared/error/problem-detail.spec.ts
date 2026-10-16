import { isHttpErrorLike, isUnauthorized, problemCode, problemDetail } from './problem-detail';

function httpError(status: number, body: unknown): unknown {
  return { status, error: body };
}

describe('problem-detail', () => {
  it('reads the stable code and the detail from the ProblemDetail body', () => {
    expect(problemCode(httpError(422, { code: 'COUPON_EXPIRED' }))).toBe('COUPON_EXPIRED');
    expect(problemDetail(httpError(422, { detail: 'El cupón ha caducado' }))).toBe(
      'El cupón ha caducado',
    );
  });

  it('ignores a missing or non-string code/detail', () => {
    expect(problemCode(httpError(0, null))).toBeNull();
    expect(problemCode(httpError(500, { code: 42 }))).toBeNull();
    expect(problemCode('boom')).toBeNull();
    expect(problemDetail(httpError(500, { detail: '   ' }))).toBeNull();
    expect(problemDetail(httpError(500, { detail: 7 }))).toBeNull();
    expect(problemDetail(undefined)).toBeNull();
  });

  it('recognises the shape of a transport error', () => {
    expect(isHttpErrorLike(httpError(500, null))).toBe(true);
    expect(isHttpErrorLike({ detail: 'x' })).toBe(false);
    expect(isHttpErrorLike('nope')).toBe(false);
    expect(isHttpErrorLike(null)).toBe(false);
  });

  it('detects an unauthorized error', () => {
    expect(isUnauthorized(httpError(401, { code: 'UNAUTHORIZED' }))).toBe(true);
    expect(isUnauthorized(httpError(409, { code: 'INSUFFICIENT_STOCK' }))).toBe(false);
    expect(isUnauthorized(null)).toBe(false);
  });
});
