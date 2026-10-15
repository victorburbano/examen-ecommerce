import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { categoryLabel } from '../category-label';
import { Product } from '../product.model';

/**
 * Tarjeta de producto. Componente "tonto": recibe el producto y la cantidad que ya tiene el
 * carrito, y emite intenciones. No conoce el carrito ni hace HTTP.
 */
@Component({
  selector: 'app-product-card',
  imports: [CurrencyPipe],
  templateUrl: './product-card.html',
  styleUrl: './product-card.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductCard {
  readonly product = input.required<Product>();
  /** Unidades que el carrito tiene de este producto. */
  readonly quantity = input(0);

  readonly add = output<Product>();
  readonly remove = output<Product>();

  protected readonly category = computed(() => categoryLabel(this.product().category));
  protected readonly soldOut = computed(() => this.product().stock <= 0);
  protected readonly atLimit = computed(() => this.quantity() >= this.product().stock);
}
