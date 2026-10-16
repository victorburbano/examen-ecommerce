import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Order, PageResult } from '../checkout/checkout.models';

/** Acceso HTTP a las órdenes. El alcance (propias o todas) lo decide el backend según el rol. */
@Injectable({ providedIn: 'root' })
export class OrdersApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/orders';

  list(page: number, size: number): Observable<PageResult<Order>> {
    return this.http.get<PageResult<Order>>(this.baseUrl, { params: { page, size } });
  }
}
