import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AuthStore } from '../../../core/auth/auth.store';
import { Header } from './header';

describe('Header', () => {
  const authenticated = signal(false);
  const email = signal<string | null>(null);
  const logout = jest.fn(() => of(undefined));

  beforeEach(() => {
    authenticated.set(false);
    email.set(null);
    logout.mockClear();
    TestBed.configureTestingModule({
      imports: [Header],
      providers: [
        provideRouter([]),
        {
          provide: AuthStore,
          useValue: { isAuthenticated: authenticated, email, logout } satisfies Partial<AuthStore>,
        },
      ],
    });
  });

  it('shows the login links when there is no session', async () => {
    const fixture = TestBed.createComponent(Header);
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Entrar');
    expect(fixture.nativeElement.textContent).toContain('Crear cuenta');
  });

  it('shows the email and a logout button with a session', async () => {
    authenticated.set(true);
    email.set('user@example.com');

    const fixture = TestBed.createComponent(Header);
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('user@example.com');
    expect(fixture.nativeElement.querySelector('button.logout')).not.toBeNull();
  });

  it('logs out when clicking Salir', async () => {
    authenticated.set(true);
    email.set('user@example.com');

    const fixture = TestBed.createComponent(Header);
    await fixture.whenStable();
    (fixture.nativeElement.querySelector('button.logout') as HTMLButtonElement).click();

    expect(logout).toHaveBeenCalled();
  });
});
