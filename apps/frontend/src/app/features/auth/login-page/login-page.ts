import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormField, email, form, required, submit } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { AuthStore } from '../../../core/auth/auth.store';
import { Credentials } from '../../../core/auth/auth.models';

/**
 * Página de login. Si llega un `returnUrl` (p. ej. desde el checkout), vuelve ahí tras entrar.
 */
@Component({
  selector: 'app-login-page',
  imports: [FormField, RouterLink],
  templateUrl: './login-page.html',
  styleUrl: './login-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPage {
  private readonly store = inject(AuthStore);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly model = signal<Credentials>({ email: '', password: '' });

  protected readonly loginForm = form(this.model, (path) => {
    required(path.email, { message: 'El email es obligatorio' });
    email(path.email, { message: 'Introduce un email válido' });
    required(path.password, { message: 'La contraseña es obligatoria' });
  });

  protected readonly error = this.store.error;
  protected readonly busy = this.store.busy;

  protected onSubmit(event: Event): void {
    event.preventDefault();
    void submit(this.loginForm, {
      action: async () => {
        const { email: userEmail, password } = this.model();
        const ok = await firstValueFrom(this.store.login(userEmail, password));
        if (ok) {
          await this.router.navigateByUrl(this.returnUrl());
        }
      },
    });
  }

  /** Ruta interna a la que volver tras el login; por defecto la home. Evita redirecciones externas. */
  private returnUrl(): string {
    const requested: string = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/';
    return requested.startsWith('/') && !requested.startsWith('//') ? requested : '/';
  }
}
