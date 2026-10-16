import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/**
 * Banner del tope (HU 4). Se muestra mientras `capApplied` sea true y desaparece cuando el carrito
 * deja de alcanzarlo. Es un componente "tonto": recibe el booleano y pinta el texto exacto.
 */
@Component({
  selector: 'app-cap-alert',
  templateUrl: './cap-alert.html',
  styleUrl: './cap-alert.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CapAlert {
  readonly visible = input.required<boolean>();
}
