import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Product } from '../../catalog/product.model';
import { CartStore } from '../cart.store';
import { CartPanel } from './cart-panel';

const HEADPHONES: Product = {
  id: 1,
  name: 'Auriculares Bluetooth',
  price: '59.90',
  category: 'TECHNOLOGY',
  stock: 10,
};
const MONITOR: Product = { id: 3, name: 'Monitor 27"', price: '249.00', category: 'TECHNOLOGY', stock: 3 };

describe('CartPanel', () => {
  let store: CartStore;

  async function createPanel(): Promise<ComponentFixture<CartPanel>> {
    TestBed.configureTestingModule({ imports: [CartPanel] });
    store = TestBed.inject(CartStore);
    const fixture = TestBed.createComponent(CartPanel);
    await fixture.whenStable();
    return fixture;
  }

  function button(fixture: ComponentFixture<CartPanel>, label: string): HTMLButtonElement {
    const buttons = Array.from(
      fixture.nativeElement.querySelectorAll('button'),
    ) as HTMLButtonElement[];
    const found = buttons.find((element) => element.getAttribute('aria-label') === label);
    if (!found) {
      throw new Error(`Button not found: ${label}`);
    }
    return found;
  }

  it('shows the empty message', async () => {
    const fixture = await createPanel();

    expect(fixture.nativeElement.textContent).toContain('El carrito está vacío');
  });

  it('renders the lines with their unit price, quantity and totals', async () => {
    const fixture = await createPanel();
    store.add(HEADPHONES, 2);
    store.add(MONITOR);
    await fixture.whenStable();

    const content: string = fixture.nativeElement.textContent;

    expect(content).toContain('Auriculares Bluetooth');
    expect(content).toContain('59.90');
    expect(content).toContain('249.00');
    expect(content).toContain('3 art.');
    // 59.90 x 2 + 249.00 = 368.80
    expect(content).toContain('368.80');
    expect(content).toContain('119.80');
  });

  it('adds one unit from the panel', async () => {
    const fixture = await createPanel();
    store.add(HEADPHONES);
    await fixture.whenStable();

    button(fixture, 'Añadir una unidad de Auriculares Bluetooth').click();
    await fixture.whenStable();

    expect(store.quantityOf(1)).toBe(2);
  });

  it('removes one unit and empties the line at zero', async () => {
    const fixture = await createPanel();
    store.add(HEADPHONES);
    await fixture.whenStable();

    button(fixture, 'Quitar una unidad de Auriculares Bluetooth').click();
    await fixture.whenStable();

    expect(store.isEmpty()).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('El carrito está vacío');
  });

  it('cannot add more units than the stock', async () => {
    const fixture = await createPanel();
    store.add(MONITOR, 3);
    await fixture.whenStable();

    expect(button(fixture, 'Añadir una unidad de Monitor 27"').disabled).toBe(true);
  });

  it('removes a whole line with Eliminar', async () => {
    const fixture = await createPanel();
    store.add(HEADPHONES);
    await fixture.whenStable();

    (fixture.nativeElement.querySelector('.line .link') as HTMLButtonElement).click();
    await fixture.whenStable();

    expect(store.isEmpty()).toBe(true);
  });

  it('clears the cart', async () => {
    const fixture = await createPanel();
    store.add(HEADPHONES);
    store.add(MONITOR);
    await fixture.whenStable();

    (fixture.nativeElement.querySelector('.cart__foot .link') as HTMLButtonElement).click();
    await fixture.whenStable();

    expect(store.isEmpty()).toBe(true);
    expect(store.subtotalCents()).toBe(0);
    expect(fixture.nativeElement.textContent).toContain('El carrito está vacío');
  });
});
