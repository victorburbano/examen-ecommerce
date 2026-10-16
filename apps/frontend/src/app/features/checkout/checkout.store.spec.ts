import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { CartStore } from '../cart/cart.store';
import { Product } from '../catalog/product.model';
import { CheckoutStore } from './checkout.store';
import { Breakdown, Order } from './checkout.models';

const MONITOR: Product = { id: 1, name: 'Monitor 27"', price: '80.00', category: 'TECHNOLOGY', stock: 10 };
const LAMP: Product = { id: 2, name: 'Lámpara', price: '50.00', category: 'HOME', stock: 5 };

const BASE: Breakdown = {
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
  discounts: [
    { type: 'CATEGORY', label: 'Tecnología 10 %', percent: '10.00', amount: '8.00' },
  ],
  originalSubtotal: '80.00',
  totalDiscount: '8.00',
  effectiveDiscountPercent: '10.00',
  total: '72.00',
  capApplied: false,
  couponCode: null,
};

function breakdown(overrides: Partial<Breakdown> = {}): Breakdown {
  return { ...BASE, ...overrides, items: BASE.items, discounts: BASE.discounts };
}

const ORDER: Order = {
  ...BASE,
  orderId: '55555555-5555-5555-5555-555555555555',
  createdAt: '2026-06-15T10:15:30Z',
};

describe('CheckoutStore', () => {
  let store: CheckoutStore;
  let cart: CartStore;
  let httpMock: HttpTestingController;
  let navigate: jest.SpyInstance;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    store = TestBed.inject(CheckoutStore);
    cart = TestBed.inject(CartStore);
    httpMock = TestBed.inject(HttpTestingController);
    navigate = jest.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
  });

  afterEach(() => httpMock.verify());

  it('starts idle with no checkout and no request', () => {
    TestBed.tick();

    expect(store.status()).toBe('idle');
    expect(store.quote()).toBeNull();
    expect(store.hasQuote()).toBe(false);
    expect(store.capApplied()).toBe(false);
    httpMock.expectNone('/api/checkout/quote');
  });

  it('quotes the cart when it changes', () => {
    cart.add(MONITOR, 2);
    TestBed.tick();

    const request = httpMock.expectOne('/api/checkout/quote');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ items: [{ productId: 1, quantity: 2 }], couponCode: null });
    request.flush(breakdown());

    expect(store.status()).toBe('ready');
    expect(store.hasQuote()).toBe(true);
    expect(store.quote()?.total).toBe('72.00');
  });

  it('re-quotes when the cart changes again', () => {
    cart.add(MONITOR, 1);
    TestBed.tick();
    httpMock.expectOne('/api/checkout/quote').flush(breakdown());

    cart.add(LAMP, 1);
    TestBed.tick();

    const request = httpMock.expectOne('/api/checkout/quote');
    expect(request.request.body).toEqual({
      items: [
        { productId: 1, quantity: 1 },
        { productId: 2, quantity: 1 },
      ],
      couponCode: null,
    });
    request.flush(breakdown({ total: '122.00' }));

    expect(store.quote()?.total).toBe('122.00');
  });

  it('ignores a stale response that arrives after a newer one', () => {
    cart.add(MONITOR, 1);
    TestBed.tick();
    const first = httpMock.expectOne(
      (req) => (req.body as { items: unknown[] }).items.length === 1,
    );

    cart.add(LAMP, 1);
    TestBed.tick();
    const second = httpMock.expectOne(
      (req) => (req.body as { items: unknown[] }).items.length === 2,
    );

    // La respuesta nueva llega primero; la vieja se descarta.
    second.flush(breakdown({ total: '122.00' }));
    first.flush(breakdown({ total: '72.00' }));

    expect(store.quote()?.total).toBe('122.00');
  });

  it('ignores a stale error that arrives after a newer quote', () => {
    cart.add(MONITOR, 1);
    TestBed.tick();
    const first = httpMock.expectOne(
      (req) => (req.body as { items: unknown[] }).items.length === 1,
    );

    cart.add(LAMP, 1);
    TestBed.tick();
    const second = httpMock.expectOne(
      (req) => (req.body as { items: unknown[] }).items.length === 2,
    );

    second.flush(breakdown({ total: '122.00' }));
    first.flush({ code: 'PRODUCT_NOT_FOUND', detail: 'gone' }, { status: 404, statusText: 'Not Found' });

    expect(store.status()).toBe('ready');
    expect(store.error()).toBeNull();
    expect(store.quote()?.total).toBe('122.00');
  });

  it('clears the breakdown and the banner when the cart empties', () => {
    cart.add(MONITOR, 1);
    TestBed.tick();
    httpMock.expectOne('/api/checkout/quote').flush(breakdown({ capApplied: true }));
    expect(store.capApplied()).toBe(true);

    cart.clear();
    TestBed.tick();

    expect(store.status()).toBe('idle');
    expect(store.quote()).toBeNull();
    expect(store.capApplied()).toBe(false);
    httpMock.expectNone('/api/checkout/quote');
  });

  it('applies a coupon only when the backend accepts it', () => {
    cart.add(MONITOR, 1);
    TestBed.tick();
    httpMock.expectOne('/api/checkout/quote').flush(breakdown());

    store.applyCoupon(' welcome2026 ');
    const request = httpMock.expectOne('/api/checkout/quote');
    expect(request.request.body).toEqual({ items: [{ productId: 1, quantity: 1 }], couponCode: 'WELCOME2026' });
    request.flush(breakdown({ couponCode: 'WELCOME2026', total: '61.20' }));

    expect(store.coupon()).toBe('WELCOME2026');
    expect(store.couponError()).toBeNull();
    expect(store.quote()?.total).toBe('61.20');
  });

  it('keeps the previous breakdown and reports the error when a coupon is rejected', () => {
    cart.add(MONITOR, 1);
    TestBed.tick();
    httpMock.expectOne('/api/checkout/quote').flush(breakdown());

    store.applyCoupon('NOPE');
    httpMock
      .expectOne('/api/checkout/quote')
      .flush({ code: 'COUPON_NOT_FOUND', detail: 'The coupon does not exist' }, { status: 422, statusText: 'Unprocessable' });

    expect(store.coupon()).toBeNull();
    expect(store.couponError()).toBe('El cupón no existe.');
    expect(store.status()).toBe('ready');
    expect(store.quote()?.total).toBe('72.00'); // el desglose previo se conserva
  });

  it('ignores an empty coupon and a coupon with an empty cart', () => {
    store.applyCoupon('   ');
    httpMock.expectNone('/api/checkout/quote');

    cart.add(MONITOR, 1);
    TestBed.tick();
    httpMock.expectOne('/api/checkout/quote').flush(breakdown());

    cart.clear();
    TestBed.tick();
    store.applyCoupon('WELCOME2026');
    httpMock.expectNone('/api/checkout/quote');
  });

  it('re-quotes with the kept coupon when the cart changes', () => {
    cart.add(MONITOR, 1);
    TestBed.tick();
    httpMock.expectOne('/api/checkout/quote').flush(breakdown());
    store.applyCoupon('WELCOME2026');
    httpMock.expectOne('/api/checkout/quote').flush(breakdown({ couponCode: 'WELCOME2026' }));

    cart.add(LAMP, 1);
    TestBed.tick();

    const request = httpMock.expectOne('/api/checkout/quote');
    expect(request.request.body).toEqual({
      items: [
        { productId: 1, quantity: 1 },
        { productId: 2, quantity: 1 },
      ],
      couponCode: 'WELCOME2026',
    });
    request.flush(breakdown({ couponCode: 'WELCOME2026', total: '105.00' }));
    expect(store.coupon()).toBe('WELCOME2026');
  });

  it('shows a quote error and clears the breakdown on a failed re-quote', () => {
    cart.add(MONITOR, 1);
    TestBed.tick();
    httpMock.expectOne('/api/checkout/quote').flush(breakdown());

    cart.add(LAMP, 1);
    TestBed.tick();
    httpMock
      .expectOne('/api/checkout/quote')
      .flush({ code: 'PRODUCT_NOT_FOUND', detail: 'gone' }, { status: 404, statusText: 'Not Found' });

    expect(store.status()).toBe('error');
    expect(store.error()).toBe('Uno de los productos ya no está disponible.');
    expect(store.quote()).toBeNull();
  });

  it('places the order and stores the confirmation', async () => {
    cart.add(MONITOR, 1);
    TestBed.tick();
    httpMock.expectOne('/api/checkout/quote').flush(breakdown());

    const result = firstValueFrom(store.placeOrder());
    const request = httpMock.expectOne('/api/checkout/orders');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ items: [{ productId: 1, quantity: 1 }], couponCode: null });
    request.flush(ORDER);

    await expect(result).resolves.toEqual(ORDER);
    expect(store.lastOrder()).toEqual(ORDER);
    expect(store.placing()).toBe(false);

    store.dismissOrder();
    expect(store.lastOrder()).toBeNull();
  });

  it('flags a stock conflict on 409', async () => {
    cart.add(MONITOR, 1);
    TestBed.tick();
    httpMock.expectOne('/api/checkout/quote').flush(breakdown());

    const result = firstValueFrom(store.placeOrder());
    httpMock
      .expectOne('/api/checkout/orders')
      .flush({ code: 'INSUFFICIENT_STOCK', detail: 'There is not enough stock' }, { status: 409, statusText: 'Conflict' });

    await expect(result).resolves.toBeNull();
    expect(store.stockConflict()).toBe(true);
    expect(store.error()).toBe('No hay stock suficiente para completar la compra.');
    expect(navigate).not.toHaveBeenCalled();
  });

  it('redirects to the login on a 401 while buying', async () => {
    cart.add(MONITOR, 1);
    TestBed.tick();
    httpMock.expectOne('/api/checkout/quote').flush(breakdown());

    const result = firstValueFrom(store.placeOrder());
    httpMock
      .expectOne('/api/checkout/orders')
      .flush({ code: 'UNAUTHORIZED' }, { status: 401, statusText: 'Unauthorized' });

    await expect(result).resolves.toBeNull();
    expect(navigate).toHaveBeenCalledWith(['/login'], { queryParams: { returnUrl: '/' } });
  });

  it('only places one order on a double click', async () => {
    cart.add(MONITOR, 1);
    TestBed.tick();
    httpMock.expectOne('/api/checkout/quote').flush(breakdown());

    const first = firstValueFrom(store.placeOrder());
    const second = firstValueFrom(store.placeOrder());

    const request = httpMock.expectOne('/api/checkout/orders');
    request.flush(ORDER);

    await expect(first).resolves.toEqual(ORDER);
    await expect(second).resolves.toBeNull();
  });

  it('reports a coupon error even without a previous breakdown', () => {
    cart.add(MONITOR, 1);
    TestBed.tick();
    httpMock
      .expectOne('/api/checkout/quote')
      .flush({ code: 'PRODUCT_NOT_FOUND', detail: 'gone' }, { status: 404, statusText: 'Not Found' });
    expect(store.hasQuote()).toBe(false);
    expect(store.status()).toBe('error');

    store.applyCoupon('WELCOME2026');
    httpMock
      .expectOne('/api/checkout/quote')
      .flush({ code: 'COUPON_NOT_FOUND' }, { status: 422, statusText: 'Unprocessable' });

    expect(store.couponError()).toBe('El cupón no existe.');
    expect(store.status()).toBe('error');
  });

  it('reports a generic order error without a stock conflict', async () => {
    cart.add(MONITOR, 1);
    TestBed.tick();
    httpMock.expectOne('/api/checkout/quote').flush(breakdown());

    const result = firstValueFrom(store.placeOrder());
    httpMock
      .expectOne('/api/checkout/orders')
      .flush({ code: 'INTERNAL_ERROR', detail: 'boom' }, { status: 500, statusText: 'Server Error' });

    await expect(result).resolves.toBeNull();
    expect(store.error()).toBe('boom');
    expect(store.stockConflict()).toBe(false);
  });

  it('does not buy an empty cart', async () => {
    TestBed.tick();

    await expect(firstValueFrom(store.placeOrder())).resolves.toBeNull();
    httpMock.expectNone('/api/checkout/orders');
  });
});
