import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormField, form, required, submit } from '@angular/forms/signals';
import { CheckoutStore } from '../checkout.store';

/** Modelo del formulario de cupón. */
interface CouponModel {
  readonly code: string;
}

/** Formulario para aplicar un cupón. El backend valida: aquí solo se dispara y se pinta el error. */
@Component({
  selector: 'app-coupon-form',
  imports: [FormField],
  templateUrl: './coupon-form.html',
  styleUrl: './coupon-form.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CouponForm {
  private readonly store = inject(CheckoutStore);

  protected readonly model = signal<CouponModel>({ code: '' });

  protected readonly couponForm = form(this.model, (path) => {
    required(path.code, { message: 'Introduce un código de cupón' });
  });

  protected readonly couponError = this.store.couponError;
  protected readonly appliedCoupon = this.store.coupon;

  protected onSubmit(event: Event): void {
    event.preventDefault();
    void submit(this.couponForm, {
      action: async () => {
        this.store.applyCoupon(this.model().code);
      },
    });
  }
}
