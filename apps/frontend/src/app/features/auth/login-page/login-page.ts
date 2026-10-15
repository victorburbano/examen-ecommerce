import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormField, email, form, required, submit } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { AuthStore } from '../../../core/auth/auth.store';
import { Credentials } from '../../../core/auth/auth.models';

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
          await this.router.navigate(['/']);
        }
      },
    });
  }
}
