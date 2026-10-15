import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ProductsApiService } from './products-api.service';

describe('ProductsApiService', () => {
  let service: ProductsApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ProductsApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('lists the catalog from a public endpoint', () => {
    let received: readonly { readonly id: number }[] | undefined;
    service.list().subscribe((products) => (received = products));

    const request = httpMock.expectOne('/api/products');
    expect(request.request.method).toBe('GET');
    request.flush([{ id: 1, name: 'Auriculares', price: '59.90', category: 'TECHNOLOGY', stock: 10 }]);

    expect(received).toHaveLength(1);
  });
});
