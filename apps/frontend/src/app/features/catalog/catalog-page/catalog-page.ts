import { ChangeDetectionStrategy, Component, inject, resource } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { CartStore } from '../../cart/cart.store';
import { CartPanel } from '../../cart/cart-panel/cart-panel';
import { ProductCard } from '../product-card/product-card';
import { ProductsApiService } from '../products-api.service';

/**
 * Página principal: catálogo + carrito. El estado de la carga (loading/error/empty) sale del
 * `resource`, que además permite reintentar. El subtotal se recalcula solo, en el carrito.
 */
@Component({
  selector: 'app-catalog-page',
  imports: [ProductCard, CartPanel],
  templateUrl: './catalog-page.html',
  styleUrl: './catalog-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CatalogPage {
  private readonly productsApi = inject(ProductsApiService);
  protected readonly cart = inject(CartStore);

  protected readonly products = resource({
    loader: () => firstValueFrom(this.productsApi.list()),
  });
}
