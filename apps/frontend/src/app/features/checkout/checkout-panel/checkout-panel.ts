import { ChangeDetectionStrategy, Component, inject, output } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthStore } from '../../../core/auth/auth.store';
import { CartStore } from '../../cart/cart.store';
import { BreakdownTable } from '../breakdown-table/breakdown-table';
import { CapAlert } from '../cap-alert/cap-alert';
import { CheckoutStore } from '../checkout.store';
import { CouponForm } from '../coupon-form/coupon-form';
import { OrderConfirmation } from '../order-confirmation/order-confirmation';

/**
 * Panel de checkout sobre el carrito: cupón, desglose del backend, banner del tope y compra.
 * Tras comprar vacía el carrito y avisa (`ordered`) para refrescar el stock; si el backend
 * responde 409, avisa (`stockConflict`) para que el catálogo se recargue.
 */
@Component({
  selector: 'app-checkout-panel',
  imports: [BreakdownTable, CapAlert, CouponForm, OrderConfirmation],
  templateUrl: './checkout-panel.html',
  styleUrl: './checkout-panel.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CheckoutPanel {
  protected readonly cart = inject(CartStore);
  protected readonly store = inject(CheckoutStore);
  private readonly auth = inject(AuthStore);
  private readonly router = inject(Router);

  readonly ordered = output();
  readonly stockConflict = output();

  protected async onBuy(): Promise<void> {
    if (!this.auth.isAuthenticated()) {
      await this.router.navigate(['/login'], { queryParams: { returnUrl: '/' } });
      return;
    }

    const order = await firstValueFrom(this.store.placeOrder());
    if (order !== null) {
      this.cart.clear();
      this.ordered.emit();
      return;
    }
    if (this.store.stockConflict()) {
      this.stockConflict.emit();
    }
  }
}
