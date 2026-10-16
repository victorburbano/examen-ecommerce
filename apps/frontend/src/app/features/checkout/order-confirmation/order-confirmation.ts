import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Order } from '../checkout.models';

/** Confirmación tras comprar: identificador, fecha y totales. */
@Component({
  selector: 'app-order-confirmation',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './order-confirmation.html',
  styleUrl: './order-confirmation.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrderConfirmation {
  readonly order = input.required<Order>();
  readonly dismiss = output();
}
