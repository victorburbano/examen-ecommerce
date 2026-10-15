import { Injectable, computed, signal } from '@angular/core';
import { Product } from '../catalog/product.model';
import { centsToAmount, toCents } from '../../shared/money';

/** Línea del carrito: el producto (instantánea) y cuántas unidades lleva. */
export interface CartLine {
  readonly product: Product;
  readonly quantity: number;
}

/** Línea lista para pintar: añade el total de la línea (exacto en céntimos y como importe). */
export interface CartLineView extends CartLine {
  readonly lineTotalCents: number;
  readonly lineTotalAmount: number;
}

/**
 * Carrito en memoria (sobrevive a la navegación y al login; no al recargar). El estado son
 * líneas con signals; totales y contadores se derivan con `computed`, así que la UI reacciona sin
 * suscripciones. El subtotal se acumula en **céntimos enteros** para no arrastrar errores de coma
 * flotante; los descuentos los calcula siempre el backend (spec 003+).
 */
@Injectable({ providedIn: 'root' })
export class CartStore {
  private readonly cartLines = signal<readonly CartLine[]>([]);

  /** Líneas del carrito con su total calculado en céntimos. */
  readonly lines = computed<readonly CartLineView[]>(() =>
    this.cartLines().map((line) => {
      const lineTotalCents = toCents(line.product.price) * line.quantity;
      return {
        product: line.product,
        quantity: line.quantity,
        lineTotalCents,
        lineTotalAmount: centsToAmount(lineTotalCents),
      };
    }),
  );

  /** Nº total de unidades (no de líneas). */
  readonly itemCount = computed(() =>
    this.cartLines().reduce((total, line) => total + line.quantity, 0),
  );

  /** Subtotal **exacto** en céntimos. */
  readonly subtotalCents = computed(() =>
    this.cartLines().reduce((total, line) => total + toCents(line.product.price) * line.quantity, 0),
  );

  /** Subtotal como importe, solo para mostrar con `CurrencyPipe`. */
  readonly subtotalAmount = computed(() => centsToAmount(this.subtotalCents()));

  readonly isEmpty = computed(() => this.cartLines().length === 0);

  /** Cantidad de un producto por su id (0 si no está en el carrito). */
  quantityOf(productId: number): number {
    return this.cartLines().find((line) => line.product.id === productId)?.quantity ?? 0;
  }

  /** ¿Se puede añadir una unidad más de este producto? */
  canAdd(product: Product): boolean {
    return product.stock > 0 && this.quantityOf(product.id) < product.stock;
  }

  /**
   * Añade unidades sin superar nunca el stock disponible. Ignora cantidades no positivas y
   * productos sin stock. Actualiza la línea existente en lugar de duplicarla.
   */
  add(product: Product, quantity = 1): void {
    if (quantity <= 0 || product.stock <= 0) {
      return;
    }
    const current = this.quantityOf(product.id);
    const next = Math.min(current + quantity, product.stock);
    if (next === current) {
      return;
    }
    this.cartLines.update((lines) =>
      lines.some((line) => line.product.id === product.id)
        ? lines.map((line) =>
            line.product.id === product.id ? { product: line.product, quantity: next } : line,
          )
        : [...lines, { product, quantity: next }],
    );
  }

  /** Quita una unidad; al llegar a 0 la línea desaparece. No hace nada si no existe. */
  removeOne(productId: number): void {
    this.cartLines.update((lines) =>
      lines
        .map((line) =>
          line.product.id === productId ? { product: line.product, quantity: line.quantity - 1 } : line,
        )
        .filter((line) => line.quantity > 0),
    );
  }

  /** Elimina la línea completa. */
  remove(productId: number): void {
    this.cartLines.update((lines) => lines.filter((line) => line.product.id !== productId));
  }

  /** Vacía el carrito. */
  clear(): void {
    this.cartLines.set([]);
  }
}
