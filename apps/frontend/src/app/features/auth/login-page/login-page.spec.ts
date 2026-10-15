import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { AuthStore } from '../../../core/auth/auth.store';
import { LoginPage } from './login-page';

const TOKENS = { accessToken: 'jwt-value', tokenType: 'Bearer', expiresInSeconds: 900 };
const USER = { userId: 5, email: 'user@example.com', roles: ['USER'] };

describe('LoginPage', () => {
  let httpMock: HttpTestingController;

  async function createPage() {
    TestBed.configureTestingModule({
      imports: [LoginPage],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    const fixture = TestBed.createComponent(LoginPage);
    await fixture.whenStable();
    httpMock = TestBed.inject(HttpTestingController);
    return fixture;
  }

  function typeInto(element: HTMLInputElement, value: string): void {
    element.value = value;
    element.dispatchEvent(new Event('input'));
  }

  afterEach(() => httpMock.verify());

  it('renders the login form', async () => {
    const fixture = await createPage();
    const content: string = fixture.nativeElement.textContent;

    expect(content).toContain('Entrar');
    expect(fixture.nativeElement.querySelector('#login-email')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('#login-password')).not.toBeNull();
  });

  it('shows a validation error for a malformed email once touched', async () => {
    const fixture = await createPage();
    const emailInput = fixture.nativeElement.querySelector('#login-email') as HTMLInputElement;

    typeInto(emailInput, 'not-an-email');
    emailInput.dispatchEvent(new Event('blur'));
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Introduce un email válido');
  });

  it('submits the credentials and navigates to the home on success', async () => {
    const fixture = await createPage();
    const router = TestBed.inject(Router);
    const navigate = jest.spyOn(router, 'navigate').mockResolvedValue(true);

    typeInto(fixture.nativeElement.querySelector('#login-email') as HTMLInputElement, 'user@example.com');
    typeInto(fixture.nativeElement.querySelector('#login-password') as HTMLInputElement, 'User123!');
    await fixture.whenStable();

    (fixture.nativeElement.querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));
    await fixture.whenStable();

    const login = httpMock.expectOne('/api/auth/login');
    expect(login.request.body).toEqual({ email: 'user@example.com', password: 'User123!' });
    expect(login.request.withCredentials).toBe(true);
    login.flush(TOKENS);
    httpMock.expectOne('/api/auth/me').flush(USER);
    await fixture.whenStable();

    expect(TestBed.inject(AuthStore).isAuthenticated()).toBe(true);
    expect(navigate).toHaveBeenCalledWith(['/']);
  });

  it('shows the API error when the credentials are rejected', async () => {
    const fixture = await createPage();
    const navigate = jest.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    typeInto(fixture.nativeElement.querySelector('#login-email') as HTMLInputElement, 'user@example.com');
    typeInto(fixture.nativeElement.querySelector('#login-password') as HTMLInputElement, 'WrongPassword!');
    await fixture.whenStable();

    (fixture.nativeElement.querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));
    await fixture.whenStable();

    httpMock
      .expectOne('/api/auth/login')
      .flush({ detail: 'Invalid email or password' }, { status: 401, statusText: 'Unauthorized' });
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Invalid email or password');
    expect(navigate).not.toHaveBeenCalled();
  });
});
