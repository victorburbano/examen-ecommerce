import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, resource, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { AuthStore } from '../../../core/auth/auth.store';
import { Order } from '../../checkout/checkout.models';
import { OrdersApiService } from '../orders-api.service';

/** Órdenes del usuario: `USER` ve las suyas y `ADMIN` todas (el backend aplica el alcance). */
@Component({
  selector: 'app-orders-page',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './orders-page.html',
  styleUrl: './orders-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrdersPage {
  private readonly api = inject(OrdersApiService);
  protected readonly auth = inject(AuthStore);

  protected readonly pageSize = 10;
  protected readonly page = signal(0);

  protected readonly orders = resource({
    params: () => this.page(),
    loader: ({ params }) => firstValueFrom(this.api.list(params, this.pageSize)),
  });

  protected readonly title = computed(() =>
    this.auth.isAdmin() ? 'Todas las órdenes' : 'Mis órdenes',
  );

  /** Unidades totales de la orden (solo presentación, no es un importe). */
  protected unitsOf(order: Order): number {
    return order.items.reduce((total, item) => total + item.quantity, 0);
  }

  protected prev(): void {
    this.page.update((page) => Math.max(page - 1, 0));
  }

  protected next(): void {
    this.page.update((page) => page + 1);
  }
}
