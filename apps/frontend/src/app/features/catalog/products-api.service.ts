import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Product } from './product.model';

/** Acceso HTTP al catálogo. No guarda estado: de eso se encarga el componente/carrito. */
@Injectable({ providedIn: 'root' })
export class ProductsApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/products';

  list(): Observable<readonly Product[]> {
    return this.http.get<readonly Product[]>(this.baseUrl);
  }
}
