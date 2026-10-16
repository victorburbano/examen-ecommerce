import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { AuthStore } from '../../../core/auth/auth.store';
import { CartStore } from '../../cart/cart.store';
import { Product } from '../../catalog/product.model';
import { CheckoutStore } from '../checkout.store';
import { Breakdown, Order } from '../checkout.models';
import { CheckoutPanel } from './checkout-panel';

const MONITOR: Product = { id: 1, name: 'Monitor 27"', price: '80.00', category: 'TECHNOLOGY', stock: 10 };

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
  originalSubtotal: '80.00',
  totalDiscount: '8.00',
  effectiveDiscountPercent: '10.00',
  total: '72.00',
  capApplied: false,
  couponCode: null,
};

const ORDER: Order = {
  ...BREAKDOWN,
  orderId: '55555555-5555-5555-5555-555555555555',
  createdAt: '2026-06-15T10:15:30Z',
};

describe('CheckoutPanel', () => {
  let httpMock: HttpTestingController;
  let cart: CartStore;
  let auth: AuthStore;

  function create(): ComponentFixture<CheckoutPanel> {
    TestBed.configureTestingModule({
      imports: [CheckoutPanel],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
    cart = TestBed.inject(CartStore);
    auth = TestBed.inject(AuthStore);
    TestBed.inject(CheckoutStore);
    return TestBed.createComponent(CheckoutPanel);
  }

  async function createWithQuote(): Promise<ComponentFixture<CheckoutPanel>> {
    const fixture = create();
    cart.add(MONITOR, 1);
    TestBed.tick();
    httpMock.expectOne('/api/checkout/quote').flush(BREAKDOWN);
    await fixture.whenStable();
    return fixture;
  }

  function buttonByText(fixture: ComponentFixture<CheckoutPanel>, text: string): HTMLButtonElement {
    const buttons = Array.from(
      fixture.nativeElement.querySelectorAll('button'),
    ) as HTMLButtonElement[];
    const found = buttons.find((button) => button.textContent?.includes(text));
    if (!found) {
      throw new Error(`Button not found: ${text}`);
    }
    return found;
  }

  afterEach(() => httpMock.verify());

  it('renders nothing while the cart is empty', async () => {
    const fixture = create();
    TestBed.tick();
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('.checkout')).toBeNull();
    expect(fixture.nativeElement.querySelector('app-order-confirmation')).toBeNull();
  });

  it('shows the breakdown calculated by the backend', async () => {
    const fixture = await createWithQuote();

    expect(fixture.nativeElement.querySelector('app-breakdown-table')).not.toBeNull();
    expect(fixture.nativeElement.textContent).toContain('72.00');
    expect(fixture.nativeElement.textContent).toContain('Comprar');
  });

  it('sends an anonymous buyer to the login', async () => {
    const fixture = await createWithQuote();
    const navigate = jest.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    buttonByText(fixture, 'Comprar').click();
    await fixture.whenStable();

    expect(navigate).toHaveBeenCalledWith(['/login'], { queryParams: { returnUrl: '/' } });
    httpMock.expectNone('/api/checkout/orders');
  });

  it('buys, clears the cart and confirms the order', async () => {
    const fixture = await createWithQuote();
    auth.setAccessToken('jwt');
    const ordered = jest.fn();
    fixture.componentInstance.ordered.subscribe(ordered);

    buttonByText(fixture, 'Comprar').click();
    await fixture.whenStable();

    const request = httpMock.expectOne('/api/checkout/orders');
    expect(request.request.body).toEqual({ items: [{ productId: 1, quantity: 1 }], couponCode: null });
    request.flush(ORDER);
    await fixture.whenStable();

    expect(cart.isEmpty()).toBe(true);
    expect(ordered).toHaveBeenCalled();
    expect(fixture.nativeElement.querySelector('app-order-confirmation')).not.toBeNull();

    buttonByText(fixture, 'Seguir comprando').click();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('app-order-confirmation')).toBeNull();
  });

  it('reports a stock conflict and keeps the cart', async () => {
    const fixture = await createWithQuote();
    auth.setAccessToken('jwt');
    const conflict = jest.fn();
    fixture.componentInstance.stockConflict.subscribe(conflict);

    buttonByText(fixture, 'Comprar').click();
    await fixture.whenStable();
    httpMock
      .expectOne('/api/checkout/orders')
      .flush({ code: 'INSUFFICIENT_STOCK', detail: 'no stock' }, { status: 409, statusText: 'Conflict' });
    await fixture.whenStable();

    expect(conflict).toHaveBeenCalled();
    expect(cart.isEmpty()).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('No hay stock suficiente');
  });

  it('disables the buy button while the order is in progress', async () => {
    const fixture = await createWithQuote();
    auth.setAccessToken('jwt');

    buttonByText(fixture, 'Comprar').click();
    await fixture.whenStable();

    const buyButton = buttonByText(fixture, 'Procesando');
    expect(buyButton.disabled).toBe(true);

    httpMock.expectOne('/api/checkout/orders').flush(ORDER);
    await fixture.whenStable();
  });

  it('does not emit anything on an order failure other than a stock conflict', async () => {
    const fixture = await createWithQuote();
    auth.setAccessToken('jwt');
    jest.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const ordered = jest.fn();
    const conflict = jest.fn();
    fixture.componentInstance.ordered.subscribe(ordered);
    fixture.componentInstance.stockConflict.subscribe(conflict);

    buttonByText(fixture, 'Comprar').click();
    await fixture.whenStable();
    httpMock
      .expectOne('/api/checkout/orders')
      .flush({ code: 'UNAUTHORIZED' }, { status: 401, statusText: 'Unauthorized' });
    await fixture.whenStable();

    expect(ordered).not.toHaveBeenCalled();
    expect(conflict).not.toHaveBeenCalled();
  });
});
