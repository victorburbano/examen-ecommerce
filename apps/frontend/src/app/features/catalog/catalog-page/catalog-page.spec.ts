import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CartStore } from '../../cart/cart.store';
import { Product } from '../product.model';
import { CatalogPage } from './catalog-page';

const CATALOG: readonly Product[] = [
  { id: 1, name: 'Auriculares Bluetooth', price: '59.90', category: 'TECHNOLOGY', stock: 10 },
  { id: 6, name: 'Taza de Cerámica', price: '9.99', category: 'HOME', stock: 2 },
  { id: 8, name: 'Zapatillas Running', price: '79.00', category: 'CLOTHING', stock: 0 },
];

describe('CatalogPage', () => {
  let httpMock: HttpTestingController;

  function createPage(): ComponentFixture<CatalogPage> {
    TestBed.configureTestingModule({
      imports: [CatalogPage],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(CatalogPage);
    fixture.detectChanges();
    return fixture;
  }

  afterEach(() => httpMock.verify());

  it('requests the public catalog and shows a loading state', () => {
    const fixture = createPage();

    expect(fixture.nativeElement.textContent).toContain('Cargando catálogo');

    const request = httpMock.expectOne('/api/products');
    expect(request.request.method).toBe('GET');
    request.flush(CATALOG);
  });

  it('renders a card per product once the catalog arrives', async () => {
    const fixture = createPage();
    httpMock.expectOne('/api/products').flush(CATALOG);
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('app-product-card')).toHaveLength(3);
    expect(fixture.nativeElement.textContent).toContain('Auriculares Bluetooth');
    expect(fixture.nativeElement.textContent).toContain('Tecnología');
  });

  it('shows an empty state when there are no products', async () => {
    const fixture = createPage();
    httpMock.expectOne('/api/products').flush([]);
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('No hay productos disponibles');
    expect(fixture.nativeElement.querySelectorAll('app-product-card')).toHaveLength(0);
  });

  it('shows an error state and retries the request', async () => {
    const fixture = createPage();
    httpMock.expectOne('/api/products').flush(null, { status: 500, statusText: 'Server Error' });
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('No se ha podido cargar el catálogo');

    (fixture.nativeElement.querySelector('button.retry') as HTMLButtonElement).click();
    fixture.detectChanges();
    httpMock.expectOne('/api/products').flush(CATALOG);
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Auriculares Bluetooth');
  });

  it('adds products to the cart and updates the local subtotal', async () => {
    const fixture = createPage();
    const store = TestBed.inject(CartStore);
    httpMock.expectOne('/api/products').flush(CATALOG);
    await fixture.whenStable();
    fixture.detectChanges();

    const addButtons = fixture.nativeElement.querySelectorAll(
      'app-product-card .btn--primary',
    ) as NodeListOf<HTMLButtonElement>;
    addButtons[0].click();
    await fixture.whenStable();
    addButtons[0].click();
    addButtons[1].click();
    await fixture.whenStable();

    // Añadir al carrito dispara la re-cotización del checkout (spec 005).
    for (const request of httpMock.match('/api/checkout/quote')) {
      request.flush({
        items: [],
        discounts: [],
        originalSubtotal: '129.79',
        totalDiscount: '0.00',
        effectiveDiscountPercent: '0.00',
        total: '129.79',
        capApplied: false,
        couponCode: null,
      });
    }
    await fixture.whenStable();

    expect(store.quantityOf(1)).toBe(2);
    expect(store.quantityOf(6)).toBe(1);
    // 59.90 x 2 + 9.99 = 129.79
    expect(store.subtotalCents()).toBe(12979);
    expect(fixture.nativeElement.textContent).toContain('129.79');
  });

  it('keeps the add button of a sold-out product disabled', async () => {
    const fixture = createPage();
    httpMock.expectOne('/api/products').flush(CATALOG);
    await fixture.whenStable();
    fixture.detectChanges();

    const cards = fixture.nativeElement.querySelectorAll('app-product-card');
    const soldOutButton = cards[2].querySelector('.btn--primary') as HTMLButtonElement;

    expect(soldOutButton.disabled).toBe(true);
    expect(soldOutButton.textContent).toContain('Sin stock');
  });
});
