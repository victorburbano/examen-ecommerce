import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { RegisterPage } from './register-page';

describe('RegisterPage', () => {
  let httpMock: HttpTestingController;

  async function createPage() {
    TestBed.configureTestingModule({
      imports: [RegisterPage],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    const fixture = TestBed.createComponent(RegisterPage);
    await fixture.whenStable();
    httpMock = TestBed.inject(HttpTestingController);
    return fixture;
  }

  function typeInto(element: HTMLInputElement, value: string): void {
    element.value = value;
    element.dispatchEvent(new Event('input'));
  }

  afterEach(() => httpMock.verify());

  it('renders the register form', async () => {
    const fixture = await createPage();

    expect(fixture.nativeElement.textContent).toContain('Crear cuenta');
    expect(fixture.nativeElement.querySelector('#register-email')).not.toBeNull();
  });

  it('requires a password of at least 8 characters', async () => {
    const fixture = await createPage();
    const passwordInput = fixture.nativeElement.querySelector('#register-password') as HTMLInputElement;

    typeInto(passwordInput, 'short');
    passwordInput.dispatchEvent(new Event('blur'));
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('al menos 8 caracteres');
  });

  it('registers the user and navigates to the login', async () => {
    const fixture = await createPage();
    const navigate = jest.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    typeInto(fixture.nativeElement.querySelector('#register-email') as HTMLInputElement, 'new@example.com');
    typeInto(fixture.nativeElement.querySelector('#register-password') as HTMLInputElement, 'User123!');
    await fixture.whenStable();

    (fixture.nativeElement.querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));
    await fixture.whenStable();

    const request = httpMock.expectOne('/api/auth/register');
    expect(request.request.body).toEqual({ email: 'new@example.com', password: 'User123!' });
    request.flush({ id: 9, email: 'new@example.com', role: 'USER' });
    await fixture.whenStable();

    expect(navigate).toHaveBeenCalledWith(['/login']);
  });

  it('shows the API error when the email is already taken', async () => {
    const fixture = await createPage();
    const navigate = jest.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    typeInto(fixture.nativeElement.querySelector('#register-email') as HTMLInputElement, 'user@example.com');
    typeInto(fixture.nativeElement.querySelector('#register-password') as HTMLInputElement, 'User123!');
    await fixture.whenStable();

    (fixture.nativeElement.querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));
    await fixture.whenStable();

    httpMock
      .expectOne('/api/auth/register')
      .flush({ detail: 'Email is already registered' }, { status: 409, statusText: 'Conflict' });
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Email is already registered');
    expect(navigate).not.toHaveBeenCalled();
  });
});
