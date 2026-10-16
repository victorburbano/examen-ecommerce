import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { OrdersApiService } from './orders-api.service';
import { Order, PageResult } from '../checkout/checkout.models';

const ORDER: Order = {
  orderId: '55555555-5555-5555-5555-555555555555',
  createdAt: '2026-06-15T10:15:30Z',
  items: [
    {
      productId: 1,
      name: 'Monitor 27"',
      category: 'TECHNOLOGY',
      unitPrice: '80.00',
      quantity: 1,
      lineTotal: '80.00',
    },
  ],
  discounts: [],
  originalSubtotal: '80.00',
  totalDiscount: '0.00',
  effectiveDiscountPercent: '0.00',
  total: '80.00',
  capApplied: false,
  couponCode: null,
};

const PAGE: PageResult<Order> = {
  content: [ORDER],
  page: 2,
  size: 10,
  totalElements: 21,
  totalPages: 3,
};

describe('OrdersApiService', () => {
  let service: OrdersApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(OrdersApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('GETs the orders page with the pagination params', () => {
    let result: PageResult<Order> | undefined;
    service.list(2, 10).subscribe((value) => (result = value));

    const request = httpMock.expectOne((req) => req.url === '/api/orders');
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('size')).toBe('10');
    request.flush(PAGE);

    expect(result).toEqual(PAGE);
  });
});
