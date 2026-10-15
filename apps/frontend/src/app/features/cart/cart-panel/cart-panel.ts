import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { CartStore } from '../cart.store';

/**
 * Panel del carrito: lista las líneas con sus totales y permite ajustar cantidades, eliminar
 * líneas y vaciar. Todo el estado viene de `CartStore`.
 */
@Component({
  selector: 'app-cart-panel',
  imports: [CurrencyPipe],
  templateUrl: './cart-panel.html',
  styleUrl: './cart-panel.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CartPanel {
  protected readonly cart = inject(CartStore);
}
