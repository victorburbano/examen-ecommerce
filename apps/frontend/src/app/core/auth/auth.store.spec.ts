import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { AuthStore } from './auth.store';

const TOKENS = { accessToken: 'jwt-value', tokenType: 'Bearer', expiresInSeconds: 900 };
const USER = { userId: 5, email: 'user@example.com', roles: ['USER'] as const };

describe('AuthStore', () => {
  let store: AuthStore;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    store = TestBed.inject(AuthStore);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => httpMock.verify());

  it('starts with no session', () => {
    expect(store.isAuthenticated()).toBe(false);
    expect(store.token()).toBeNull();
    expect(store.user()).toBeNull();
    expect(store.email()).toBeNull();
    expect(store.roles()).toEqual([]);
    expect(store.isAdmin()).toBe(false);
  });

  it('logs in keeping the access token in memory and loading the user', () => {
    let result: boolean | undefined;
    store.login('user@example.com', 'User123!').subscribe((value) => (result = value));

    httpMock.expectOne('/api/auth/login').flush(TOKENS);
    httpMock.expectOne('/api/auth/me').flush(USER);

    expect(result).toBe(true);
    expect(store.token()).toBe('jwt-value');
    expect(store.email()).toBe('user@example.com');
    expect(store.isAuthenticated()).toBe(true);
    expect(store.isAdmin()).toBe(false);
  });

  it('reports admin sessions', () => {
    store.login('admin@example.com', 'Admin123!').subscribe();

    httpMock.expectOne('/api/auth/login').flush(TOKENS);
    httpMock.expectOne('/api/auth/me').flush({ ...USER, roles: ['ADMIN'] });

    expect(store.isAdmin()).toBe(true);
  });

  it('exposes the ProblemDetail message and clears the session when login fails', () => {
    let result: boolean | undefined;
    store.login('user@example.com', 'wrong').subscribe((value) => (result = value));

    httpMock
      .expectOne('/api/auth/login')
      .flush({ detail: 'Invalid email or password' }, { status: 401, statusText: 'Unauthorized' });

    expect(result).toBe(false);
    expect(store.error()).toBe('Invalid email or password');
    expect(store.token()).toBeNull();
  });

  it('registers a user without opening a session', () => {
    let result: boolean | undefined;
    store.register('new@example.com', 'User123!').subscribe((value) => (result = value));

    httpMock.expectOne('/api/auth/register').flush({ id: 9, email: 'new@example.com', role: 'USER' });

    expect(result).toBe(true);
    expect(store.isAuthenticated()).toBe(false);
  });

  it('reports an error when registration is rejected', () => {
    let result: boolean | undefined;
    store.register('user@example.com', 'User123!').subscribe((value) => (result = value));

    httpMock
      .expectOne('/api/auth/register')
      .flush({ detail: 'Email is already registered' }, { status: 409, statusText: 'Conflict' });

    expect(result).toBe(false);
    expect(store.error()).toBe('Email is already registered');
  });

  it('restores the session from the refresh cookie', () => {
    let completed = false;
    store.restore().subscribe({ complete: () => (completed = true) });

    const refresh = httpMock.expectOne('/api/auth/refresh');
    expect(refresh.request.withCredentials).toBe(true);
    refresh.flush(TOKENS);
    httpMock.expectOne('/api/auth/me').flush(USER);

    expect(completed).toBe(true);
    expect(store.isAuthenticated()).toBe(true);
    expect(store.email()).toBe('user@example.com');
  });

  it('restore never fails when there is no cookie', () => {
    let completed = false;
    store.restore().subscribe({ complete: () => (completed = true) });

    httpMock
      .expectOne('/api/auth/refresh')
      .flush({ detail: 'Invalid or expired refresh token' }, { status: 401, statusText: 'Unauthorized' });

    expect(completed).toBe(true);
    expect(store.isAuthenticated()).toBe(false);
  });

  it('restore does nothing when a session already exists', () => {
    store.login('user@example.com', 'User123!').subscribe();
    httpMock.expectOne('/api/auth/login').flush(TOKENS);
    httpMock.expectOne('/api/auth/me').flush(USER);

    store.restore().subscribe();

    httpMock.expectNone('/api/auth/refresh');
  });

  it('logs out, clears the session and navigates to the login', () => {
    const navigate = jest.spyOn(router, 'navigate').mockResolvedValue(true);
    store.login('user@example.com', 'User123!').subscribe();
    httpMock.expectOne('/api/auth/login').flush(TOKENS);
    httpMock.expectOne('/api/auth/me').flush(USER);

    store.logout().subscribe();
    httpMock.expectOne('/api/auth/logout').flush(null, { status: 204, statusText: 'No Content' });

    expect(store.isAuthenticated()).toBe(false);
    expect(navigate).toHaveBeenCalledWith(['/login']);
  });

  it('logs out even when the API call fails', () => {
    jest.spyOn(router, 'navigate').mockResolvedValue(true);
    store.setAccessToken('jwt-value');

    store.logout().subscribe();
    httpMock
      .expectOne('/api/auth/logout')
      .flush(null, { status: 500, statusText: 'Server Error' });

    expect(store.isAuthenticated()).toBe(false);
  });

  it('expires the session and returns to the login', () => {
    const navigate = jest.spyOn(router, 'navigate').mockResolvedValue(true);
    store.setAccessToken('jwt-value');

    store.expireSession();

    expect(store.token()).toBeNull();
    expect(navigate).toHaveBeenCalledWith(['/login']);
  });
});
