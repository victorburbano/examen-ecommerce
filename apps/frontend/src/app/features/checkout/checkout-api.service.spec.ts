import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CheckoutApiService } from './checkout-api.service';
import { Breakdown, Order, QuoteRequest } from './checkout.models';

const REQUEST: QuoteRequest = {
  items: [{ productId: 1, quantity: 2 }],
  couponCode: 'WELCOME2026',
};

const BREAKDOWN: Breakdown = {
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
  discounts: [{ type: 'CATEGORY', label: 'Tecnología 10 %', percent: '10.00', amount: '8.00' }],
  originalSubtotal: '130.00',
  totalDiscount: '31.49',
  effectiveDiscountPercent: '24.22',
  total: '98.51',
  capApplied: false,
  couponCode: 'WELCOME2026',
};

const ORDER: Order = {
  ...BREAKDOWN,
  orderId: '55555555-5555-5555-5555-555555555555',
  createdAt: '2026-06-15T10:15:30Z',
};

describe('CheckoutApiService', () => {
  let service: CheckoutApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CheckoutApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('POSTs the quote request and returns the breakdown', () => {
    let result: Breakdown | undefined;
    service.quote(REQUEST).subscribe((value) => (result = value));

    const request = httpMock.expectOne('/api/checkout/quote');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(REQUEST);
    request.flush(BREAKDOWN);

    expect(result).toEqual(BREAKDOWN);
  });

  it('POSTs the order request and returns the created order', () => {
    let result: Order | undefined;
    service.placeOrder(REQUEST).subscribe((value) => (result = value));

    const request = httpMock.expectOne('/api/checkout/orders');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(REQUEST);
    request.flush(ORDER);

    expect(result).toEqual(ORDER);
  });
});
