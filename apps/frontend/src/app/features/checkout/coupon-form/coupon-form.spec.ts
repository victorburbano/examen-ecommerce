import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { CartStore } from '../../cart/cart.store';
import { Product } from '../../catalog/product.model';
import { CheckoutStore } from '../checkout.store';
import { Breakdown } from '../checkout.models';
import { CouponForm } from './coupon-form';

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

describe('CouponForm', () => {
  let httpMock: HttpTestingController;
  let store: CheckoutStore;

  async function create(): Promise<ComponentFixture<CouponForm>> {
    TestBed.configureTestingModule({
      imports: [CouponForm],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
    store = TestBed.inject(CheckoutStore);

    // El cupón solo se prueba con carrito: se prepara una cotización previa.
    TestBed.inject(CartStore).add(MONITOR, 1);
    TestBed.tick();
    httpMock.expectOne('/api/checkout/quote').flush(BREAKDOWN);

    const fixture = TestBed.createComponent(CouponForm);
    await fixture.whenStable();
    return fixture;
  }

  function typeInto(element: HTMLInputElement, value: string): void {
    element.value = value;
    element.dispatchEvent(new Event('input'));
  }

  afterEach(() => httpMock.verify());

  it('applies a coupon and reports it as applied', async () => {
    const fixture = await create();
    typeInto(fixture.nativeElement.querySelector('#checkout-coupon') as HTMLInputElement, 'welcome2026');
    await fixture.whenStable();

    (fixture.nativeElement.querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));
    await fixture.whenStable();

    const request = httpMock.expectOne('/api/checkout/quote');
    expect(request.request.body.couponCode).toBe('WELCOME2026');
    request.flush({ ...BREAKDOWN, couponCode: 'WELCOME2026' });
    await fixture.whenStable();

    expect(store.coupon()).toBe('WELCOME2026');
    expect(fixture.nativeElement.textContent).toContain('Cupón aplicado');
    expect(fixture.nativeElement.textContent).toContain('WELCOME2026');
  });

  it('shows the API error under the field when the coupon is rejected', async () => {
    const fixture = await create();
    typeInto(fixture.nativeElement.querySelector('#checkout-coupon') as HTMLInputElement, 'NOPE');
    await fixture.whenStable();

    (fixture.nativeElement.querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));
    await fixture.whenStable();

    httpMock
      .expectOne('/api/checkout/quote')
      .flush({ code: 'COUPON_EXPIRED', detail: 'expired' }, { status: 422, statusText: 'Unprocessable' });
    await fixture.whenStable();

    const alert = fixture.nativeElement.querySelector('.coupon__error') as HTMLElement;
    expect(alert).not.toBeNull();
    expect(alert.getAttribute('role')).toBe('alert');
    expect(alert.textContent).toContain('El cupón ha caducado.');
  });

  it('does not call the API for an empty coupon', async () => {
    const fixture = await create();

    (fixture.nativeElement.querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));
    await fixture.whenStable();

    httpMock.expectNone('/api/checkout/quote');
    expect(fixture.nativeElement.textContent).toContain('Introduce un código de cupón');
  });
});
