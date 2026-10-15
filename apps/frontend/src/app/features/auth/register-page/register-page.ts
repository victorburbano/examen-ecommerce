import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormField, email, form, minLength, required, submit } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { AuthStore } from '../../../core/auth/auth.store';
import { Credentials } from '../../../core/auth/auth.models';

@Component({
  selector: 'app-register-page',
  imports: [FormField, RouterLink],
  templateUrl: './register-page.html',
  styleUrl: './register-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterPage {
  private readonly store = inject(AuthStore);
  private readonly router = inject(Router);

  protected readonly model = signal<Credentials>({ email: '', password: '' });

  protected readonly registerForm = form(this.model, (path) => {
    required(path.email, { message: 'El email es obligatorio' });
    email(path.email, { message: 'Introduce un email válido' });
    required(path.password, { message: 'La contraseña es obligatoria' });
    minLength(path.password, 8, { message: 'La contraseña debe tener al menos 8 caracteres' });
  });

  protected readonly error = this.store.error;
  protected readonly busy = this.store.busy;

  protected onSubmit(event: Event): void {
    event.preventDefault();
    void submit(this.registerForm, {
      action: async () => {
        const { email: userEmail, password } = this.model();
        const ok = await firstValueFrom(this.store.register(userEmail, password));
        if (ok) {
          await this.router.navigate(['/login']);
        }
      },
    });
  }
}
