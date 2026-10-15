import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot, provideRouter } from '@angular/router';
import { authGuard, roleGuard } from './auth.guard';
import { AuthStore } from './auth.store';

describe('auth guards', () => {
  const authenticated = signal(false);
  const roles = signal<readonly string[]>([]);

  beforeEach(() => {
    authenticated.set(false);
    roles.set([]);
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: AuthStore,
          useValue: { isAuthenticated: authenticated, roles } satisfies Partial<AuthStore>,
        },
      ],
    });
  });

  function run(guard: CanActivateFn): unknown {
    return TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );
  }

  it('authGuard allows an authenticated session', () => {
    authenticated.set(true);

    expect(run(authGuard)).toBe(true);
  });

  it('authGuard redirects to the login when there is no session', () => {
    const router = TestBed.inject(Router);

    expect(run(authGuard)).toEqual(router.createUrlTree(['/login']));
  });

  it('roleGuard allows a session with the required role', () => {
    authenticated.set(true);
    roles.set(['ADMIN']);

    expect(run(roleGuard('ADMIN'))).toBe(true);
  });

  it('roleGuard redirects to the login without a session', () => {
    const router = TestBed.inject(Router);

    expect(run(roleGuard('ADMIN'))).toEqual(router.createUrlTree(['/login']));
  });

  it('roleGuard sends a session without the role to the home', () => {
    authenticated.set(true);
    roles.set(['USER']);
    const router = TestBed.inject(Router);

    expect(run(roleGuard('ADMIN'))).toEqual(router.createUrlTree(['/']));
  });
});
