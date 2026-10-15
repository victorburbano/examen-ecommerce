import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Product } from '../product.model';
import { ProductCard } from './product-card';

const PRODUCT: Product = {
  id: 1,
  name: 'Auriculares Bluetooth',
  price: '59.90',
  category: 'TECHNOLOGY',
  stock: 10,
};
const SOLD_OUT: Product = {
  id: 8,
  name: 'Zapatillas Running',
  price: '79.00',
  category: 'CLOTHING',
  stock: 0,
};

describe('ProductCard', () => {
  async function createCard(product: Product, quantity = 0): Promise<ComponentFixture<ProductCard>> {
    TestBed.configureTestingModule({ imports: [ProductCard] });
    const fixture = TestBed.createComponent(ProductCard);
    fixture.componentRef.setInput('product', product);
    fixture.componentRef.setInput('quantity', quantity);
    await fixture.whenStable();
    return fixture;
  }

  function addButton(fixture: ComponentFixture<ProductCard>): HTMLButtonElement {
    return fixture.nativeElement.querySelector('.btn--primary') as HTMLButtonElement;
  }

  it('renders the product with its translated category and string price', async () => {
    const fixture = await createCard(PRODUCT);
    const content: string = fixture.nativeElement.textContent;

    expect(content).toContain('Auriculares Bluetooth');
    expect(content).toContain('Tecnología');
    expect(content).toContain('59.90');
    expect(content).toContain('Stock: 10');
  });

  it('hides the quantity controls when the cart has none', async () => {
    const fixture = await createCard(PRODUCT);

    expect(fixture.nativeElement.querySelector('.btn--ghost')).toBeNull();
    expect(addButton(fixture).textContent).toContain('Agregar');
    expect(addButton(fixture).disabled).toBe(false);
  });

  it('emits the product when adding', async () => {
    const fixture = await createCard(PRODUCT);
    const added: Product[] = [];
    fixture.componentInstance.add.subscribe((product) => added.push(product));

    addButton(fixture).click();

    expect(added).toEqual([PRODUCT]);
  });

  it('shows the quantity and emits when removing one unit', async () => {
    const fixture = await createCard(PRODUCT, 2);
    const removed: Product[] = [];
    fixture.componentInstance.remove.subscribe((product) => removed.push(product));

    expect(fixture.nativeElement.textContent).toContain('2');

    (fixture.nativeElement.querySelector('.btn--ghost') as HTMLButtonElement).click();

    expect(removed).toEqual([PRODUCT]);
  });

  it('disables the add button and shows Sin stock when there is no stock', async () => {
    const fixture = await createCard(SOLD_OUT);
    const button = addButton(fixture);

    expect(fixture.nativeElement.textContent).toContain('Sin stock');
    expect(button.textContent).toContain('Sin stock');
    expect(button.disabled).toBe(true);
  });

  it('disables the add button when the cart already holds all the stock', async () => {
    const fixture = await createCard(PRODUCT, 10);

    expect(addButton(fixture).disabled).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Ya tienes todas las unidades disponibles');
  });
});
