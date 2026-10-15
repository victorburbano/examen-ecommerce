import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthApiService } from './auth-api.service';

describe('AuthApiService', () => {
  let service: AuthApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('registers a user', () => {
    service.register({ email: 'user@example.com', password: 'User123!' }).subscribe();

    const request = httpMock.expectOne('/api/auth/register');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ email: 'user@example.com', password: 'User123!' });
    request.flush({ id: 1, email: 'user@example.com', role: 'USER' });
  });

  it('logs in sending the refresh cookie', () => {
    service.login({ email: 'user@example.com', password: 'User123!' }).subscribe();

    const request = httpMock.expectOne('/api/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.withCredentials).toBe(true);
    request.flush({ accessToken: 'jwt', tokenType: 'Bearer', expiresInSeconds: 900 });
  });

  it('refreshes the session sending the cookie', () => {
    service.refresh().subscribe();

    const request = httpMock.expectOne('/api/auth/refresh');
    expect(request.request.method).toBe('POST');
    expect(request.request.withCredentials).toBe(true);
    request.flush({ accessToken: 'jwt', tokenType: 'Bearer', expiresInSeconds: 900 });
  });

  it('logs out sending the cookie', () => {
    service.logout().subscribe();

    const request = httpMock.expectOne('/api/auth/logout');
    expect(request.request.withCredentials).toBe(true);
    request.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('reads the current user', () => {
    service.me().subscribe();

    const request = httpMock.expectOne('/api/auth/me');
    expect(request.request.method).toBe('GET');
    request.flush({ userId: 1, email: 'user@example.com', roles: ['USER'] });
  });
});
