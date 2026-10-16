import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { categoryLabel } from '../../catalog/category-label';
import { Breakdown, ItemLine } from '../checkout.models';

/** Línea de ítem lista para pintar: la etiqueta de categoría ya traducida. */
interface ItemRow extends ItemLine {
  readonly categoryName: string;
}

/**
 * Tabla del desglose calculado por el backend: líneas, pasos de descuento, % efectivo, ahorro
 * total y total a pagar. No calcula nada: solo formatea los importes `string` recibidos.
 */
@Component({
  selector: 'app-breakdown-table',
  imports: [CurrencyPipe],
  templateUrl: './breakdown-table.html',
  styleUrl: './breakdown-table.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BreakdownTable {
  readonly breakdown = input.required<Breakdown>();

  protected readonly itemRows = computed<readonly ItemRow[]>(() =>
    this.breakdown().items.map((item) => ({ ...item, categoryName: categoryLabel(item.category) })),
  );
}
