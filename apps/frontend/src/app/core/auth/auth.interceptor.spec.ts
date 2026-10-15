import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { AuthStore } from './auth.store';

const TOKENS = { accessToken: 'new-jwt', tokenType: 'Bearer', expiresInSeconds: 900 };
const UNAUTHORIZED = { status: 401, statusText: 'Unauthorized' } as const;

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let store: AuthStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    store = TestBed.inject(AuthStore);
    // `expireSession()` navega al login: en el test se sustituye la navegación real.
    jest.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
  });

  afterEach(() => httpMock.verify());

  it('adds the Bearer header to private requests', () => {
    store.setAccessToken('jwt-value');

    http.get('/api/catalog/products').subscribe();
    const request = httpMock.expectOne('/api/catalog/products');

    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt-value');
    request.flush({});
  });

  it('does not add the Bearer header when there is no session', () => {
    http.get('/api/catalog/products').subscribe();
    const request = httpMock.expectOne('/api/catalog/products');

    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({});
  });

  it('does not add the Bearer header to public auth endpoints', () => {
    store.setAccessToken('jwt-value');

    http.post('/api/auth/login', {}).subscribe();
    const request = httpMock.expectOne('/api/auth/login');

    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush(TOKENS);
  });

  it('refreshes once and retries the original request on 401', () => {
    store.setAccessToken('expired-jwt');
    const received: unknown[] = [];
    http.get('/api/catalog/products').subscribe((value) => received.push(value));

    httpMock.expectOne('/api/catalog/products').flush(null, UNAUTHORIZED);

    const refresh = httpMock.expectOne('/api/auth/refresh');
    expect(refresh.request.withCredentials).toBe(true);
    refresh.flush(TOKENS);

    const retried = httpMock.expectOne('/api/catalog/products');
    expect(retried.request.headers.get('Authorization')).toBe('Bearer new-jwt');
    retried.flush({ ok: true });

    expect(received).toEqual([{ ok: true }]);
    expect(store.token()).toBe('new-jwt');
  });

  it('shares a single refresh between concurrent 401s', () => {
    store.setAccessToken('expired-jwt');
    http.get('/api/a').subscribe();
    http.get('/api/b').subscribe();

    httpMock.expectOne('/api/a').flush(null, UNAUTHORIZED);
    httpMock.expectOne('/api/b').flush(null, UNAUTHORIZED);

    // Solo se emite una petición de refresh para las dos peticiones fallidas.
    const refresh = httpMock.expectOne('/api/auth/refresh');
    refresh.flush(TOKENS);

    httpMock.expectOne('/api/a').flush({});
    httpMock.expectOne('/api/b').flush({});

    expect(store.token()).toBe('new-jwt');
  });

  it('expires the session when the refresh fails', () => {
    const expire = jest.spyOn(store, 'expireSession');
    store.setAccessToken('expired-jwt');
    let errored = false;
    http.get('/api/catalog/products').subscribe({ error: () => (errored = true) });

    httpMock.expectOne('/api/catalog/products').flush(null, UNAUTHORIZED);
    httpMock.expectOne('/api/auth/refresh').flush(null, UNAUTHORIZED);

    expect(errored).toBe(true);
    expect(expire).toHaveBeenCalled();
  });

  it('does not refresh twice for a retried request that fails again', () => {
    store.setAccessToken('expired-jwt');
    let errored = false;
    http.get('/api/catalog/products').subscribe({ error: () => (errored = true) });

    httpMock.expectOne('/api/catalog/products').flush(null, UNAUTHORIZED);
    httpMock.expectOne('/api/auth/refresh').flush(TOKENS);
    httpMock.expectOne('/api/catalog/products').flush(null, UNAUTHORIZED);

    expect(errored).toBe(true);
    httpMock.expectNone('/api/auth/refresh');
  });

  it('propagates errors that are not 401', () => {
    store.setAccessToken('jwt-value');
    let status = 0;
    http.get('/api/catalog/products').subscribe({
      error: (error: { status: number }) => (status = error.status),
    });

    httpMock
      .expectOne('/api/catalog/products')
      .flush({ detail: 'boom' }, { status: 500, statusText: 'Server Error' });

    expect(status).toBe(500);
    httpMock.expectNone('/api/auth/refresh');
  });
});
