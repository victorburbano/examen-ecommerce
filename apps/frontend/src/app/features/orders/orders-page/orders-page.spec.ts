import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { AuthStore } from '../../../core/auth/auth.store';
import { Order, PageResult } from '../../checkout/checkout.models';
import { OrdersPage } from './orders-page';

const ORDER: Order = {
  orderId: '55555555-5555-5555-5555-555555555555',
  createdAt: '2026-06-15T10:15:30Z',
  items: [
    {
      productId: 1,
      name: 'Monitor 27"',
      category: 'TECHNOLOGY',
      unitPrice: '80.00',
      quantity: 2,
      lineTotal: '160.00',
    },
  ],
  discounts: [{ type: 'CATEGORY', label: 'Tecnología 10 %', percent: '10.00', amount: '16.00' }],
  originalSubtotal: '160.00',
  totalDiscount: '16.00',
  effectiveDiscountPercent: '10.00',
  total: '144.00',
  capApplied: true,
  couponCode: 'WELCOME2026',
};

const PLAIN: Order = {
  orderId: '66666666-6666-6666-6666-666666666666',
  createdAt: '2026-06-16T09:00:00Z',
  items: [
    {
      productId: 3,
      name: 'Silla',
      category: 'HOME',
      unitPrice: '50.00',
      quantity: 1,
      lineTotal: '50.00',
    },
  ],
  discounts: [],
  originalSubtotal: '50.00',
  totalDiscount: '0.00',
  effectiveDiscountPercent: '0.00',
  total: '50.00',
  capApplied: false,
  couponCode: null,
};

function page(overrides: Partial<PageResult<Order>> = {}): PageResult<Order> {
  return { content: [ORDER, PLAIN], page: 0, size: 10, totalElements: 2, totalPages: 1, ...overrides };
}

describe('OrdersPage', () => {
  const isAdmin = signal(false);
  let httpMock: HttpTestingController;

  function createPage(): ComponentFixture<OrdersPage> {
    TestBed.configureTestingModule({
      imports: [OrdersPage],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthStore, useValue: { isAdmin } },
      ],
    });
    httpMock = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(OrdersPage);
    fixture.detectChanges();
    return fixture;
  }

  function buttonByText(fixture: ComponentFixture<OrdersPage>, text: string): HTMLButtonElement {
    const buttons = Array.from(
      fixture.nativeElement.querySelectorAll('button'),
    ) as HTMLButtonElement[];
    const found = buttons.find((button) => button.textContent?.includes(text));
    if (!found) {
      throw new Error(`Button not found: ${text}`);
    }
    return found;
  }

  beforeEach(() => isAdmin.set(false));
  afterEach(() => httpMock.verify());

  it('lists the current user orders with a loading state first', async () => {
    const fixture = createPage();

    expect(fixture.nativeElement.textContent).toContain('Cargando órdenes');

    const request = httpMock.expectOne((req) => req.url === '/api/orders');
    expect(request.request.params.get('page')).toBe('0');
    expect(request.request.params.get('size')).toBe('10');
    request.flush(page());
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Mis órdenes');
    expect(fixture.nativeElement.querySelectorAll('tbody tr')).toHaveLength(2);
    expect(fixture.nativeElement.textContent).toContain('55555555');
    expect(fixture.nativeElement.textContent).toContain('144.00');
    expect(fixture.nativeElement.textContent).toContain('tope');
  });

  it('shows the admin scope when the user is an admin', async () => {
    isAdmin.set(true);
    const fixture = createPage();
    httpMock.expectOne((req) => req.url === '/api/orders').flush(page());
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Todas las órdenes');
  });

  it('shows an empty state when there are no orders', async () => {
    const fixture = createPage();
    httpMock.expectOne((req) => req.url === '/api/orders').flush(page({ content: [], totalElements: 0, totalPages: 0 }));
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Todavía no hay órdenes');
  });

  it('pages forward and back', async () => {
    const fixture = createPage();
    httpMock
      .expectOne((req) => req.url === '/api/orders')
      .flush(page({ totalElements: 11, totalPages: 2 }));
    await fixture.whenStable();
    fixture.detectChanges();

    expect(buttonByText(fixture, 'Anterior').disabled).toBe(true);
    expect(buttonByText(fixture, 'Siguiente').disabled).toBe(false);

    buttonByText(fixture, 'Siguiente').click();
    fixture.detectChanges();
    httpMock
      .expectOne((req) => req.url === '/api/orders' && req.params.get('page') === '1')
      .flush(page({ page: 1, totalElements: 11, totalPages: 2 }));
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Página 2 de 2');
    expect(buttonByText(fixture, 'Siguiente').disabled).toBe(true);

    buttonByText(fixture, 'Anterior').click();
    fixture.detectChanges();
    httpMock
      .expectOne((req) => req.url === '/api/orders' && req.params.get('page') === '0')
      .flush(page({ totalElements: 11, totalPages: 2 }));
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Página 1 de 2');
  });

  it('shows an error and retries the request', async () => {
    const fixture = createPage();
    httpMock
      .expectOne((req) => req.url === '/api/orders')
      .flush(null, { status: 500, statusText: 'Server Error' });
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('No se han podido cargar las órdenes');

    buttonByText(fixture, 'Reintentar').click();
    fixture.detectChanges();
    httpMock.expectOne((req) => req.url === '/api/orders').flush(page());
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('55555555');
  });
});
