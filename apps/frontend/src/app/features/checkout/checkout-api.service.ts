import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Breakdown, Order, QuoteRequest } from './checkout.models';

/**
 * Llamadas HTTP del checkout. No guarda estado: de eso se encarga `CheckoutStore`.
 * `quote` es público; `orders` requiere sesión (lo decide el backend).
 */
@Injectable({ providedIn: 'root' })
export class CheckoutApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/checkout';

  quote(request: QuoteRequest): Observable<Breakdown> {
    return this.http.post<Breakdown>(`${this.baseUrl}/quote`, request);
  }

  placeOrder(request: QuoteRequest): Observable<Order> {
    return this.http.post<Order>(`${this.baseUrl}/orders`, request);
  }
}
