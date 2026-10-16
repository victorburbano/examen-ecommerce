import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
  provideRouter,
} from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthStore } from './auth.store';

describe('authGuard', () => {
  const authenticated = signal(false);

  beforeEach(() => {
    authenticated.set(false);
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: AuthStore,
          useValue: { isAuthenticated: authenticated } satisfies Partial<AuthStore>,
        },
      ],
    });
  });

  function run(guard: CanActivateFn): unknown {
    return TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );
  }

  it('allows an authenticated session', () => {
    authenticated.set(true);

    expect(run(authGuard)).toBe(true);
  });

  it('redirects to the login when there is no session', () => {
    const router = TestBed.inject(Router);

    expect(run(authGuard)).toEqual(router.createUrlTree(['/login']));
  });
});
