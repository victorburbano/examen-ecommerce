import { TestBed } from '@angular/core/testing';
import { Product } from '../catalog/product.model';
import { CartStore } from './cart.store';

const HEADPHONES: Product = {
  id: 1,
  name: 'Auriculares Bluetooth',
  price: '59.90',
  category: 'TECHNOLOGY',
  stock: 10,
};
const MONITOR: Product = { id: 3, name: 'Monitor 27"', price: '249.00', category: 'TECHNOLOGY', stock: 3 };
const SOLD_OUT: Product = {
  id: 8,
  name: 'Zapatillas Running',
  price: '79.00',
  category: 'CLOTHING',
  stock: 0,
};

describe('CartStore', () => {
  let store: CartStore;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    store = TestBed.inject(CartStore);
  });

  it('starts empty', () => {
    expect(store.isEmpty()).toBe(true);
    expect(store.lines()).toEqual([]);
    expect(store.itemCount()).toBe(0);
    expect(store.subtotalCents()).toBe(0);
    expect(store.subtotalAmount()).toBe(0);
  });

  it('adds a product as a new line', () => {
    store.add(HEADPHONES);

    expect(store.isEmpty()).toBe(false);
    expect(store.quantityOf(1)).toBe(1);
    expect(store.itemCount()).toBe(1);
    expect(store.subtotalCents()).toBe(5990);
    expect(store.subtotalAmount()).toBeCloseTo(59.9, 10);
  });

  it('updates the existing line instead of duplicating the product', () => {
    store.add(HEADPHONES);
    store.add(HEADPHONES);

    expect(store.lines()).toHaveLength(1);
    expect(store.quantityOf(1)).toBe(2);
  });

  it('updates only the affected line when the cart holds several products', () => {
    store.add(HEADPHONES);
    store.add(MONITOR);
    const headphones = store.lines()[0].product;

    store.add(MONITOR);

    expect(store.quantityOf(3)).toBe(2);
    expect(store.quantityOf(1)).toBe(1);
    expect(store.lines()[0].product).toBe(headphones);
  });

  it('adds several units at once', () => {
    store.add(HEADPHONES, 3);

    expect(store.quantityOf(1)).toBe(3);
    expect(store.subtotalCents()).toBe(17970);
  });

  it('never goes above the available stock', () => {
    store.add(MONITOR, 5);

    expect(store.quantityOf(3)).toBe(3);
    expect(store.canAdd(MONITOR)).toBe(false);
  });

  it('ignores a further add when the line is already at the limit', () => {
    store.add(MONITOR, 3);
    const before = store.lines();

    store.add(MONITOR);

    expect(store.quantityOf(3)).toBe(3);
    expect(store.lines()).toBe(before);
  });

  it('ignores sold-out products', () => {
    store.add(SOLD_OUT);

    expect(store.isEmpty()).toBe(true);
    expect(store.canAdd(SOLD_OUT)).toBe(false);
  });

  it('ignores non-positive quantities', () => {
    store.add(HEADPHONES, 0);
    store.add(HEADPHONES, -2);

    expect(store.isEmpty()).toBe(true);
  });

  it('computes each line total and the subtotal exactly', () => {
    store.add(HEADPHONES, 2);
    store.add(MONITOR);

    expect(store.lines()[0].lineTotalCents).toBe(11980);
    expect(store.lines()[0].lineTotalAmount).toBeCloseTo(119.8, 10);
    expect(store.lines()[1].lineTotalCents).toBe(24900);
    expect(store.subtotalCents()).toBe(36880);
    expect(store.subtotalAmount()).toBeCloseTo(368.8, 10);
  });

  it('adds up without floating point drift', () => {
    const cheap: Product = { id: 9, name: 'Chicle', price: '0.10', category: 'HOME', stock: 10 };

    store.add(cheap, 3);

    expect(store.subtotalCents()).toBe(30);
  });

  it('keeps the product snapshot even if it is no longer in the catalog', () => {
    store.add(HEADPHONES, 2);
    // El catálogo se recarga sin ese producto: el carrito conserva su instantánea y sus totales.
    const reloadedCatalog: readonly Product[] = [];

    expect(reloadedCatalog).not.toContain(HEADPHONES);
    expect(store.lines()[0].product).toBe(HEADPHONES);
    expect(store.subtotalCents()).toBe(11980);
  });

  it('removes one unit at a time and drops the line at zero', () => {
    store.add(HEADPHONES, 2);

    store.removeOne(1);
    expect(store.quantityOf(1)).toBe(1);

    store.removeOne(1);
    expect(store.isEmpty()).toBe(true);
    expect(store.lines()).toEqual([]);
  });

  it('does nothing when removing an unknown or negative product', () => {
    store.add(HEADPHONES);

    store.removeOne(999);
    store.remove(-1);

    expect(store.quantityOf(1)).toBe(1);
  });

  it('removes a whole line', () => {
    store.add(HEADPHONES);
    store.add(MONITOR);

    store.remove(1);

    expect(store.quantityOf(1)).toBe(0);
    expect(store.lines()).toHaveLength(1);
    expect(store.quantityOf(3)).toBe(1);
  });

  it('clears the cart', () => {
    store.add(HEADPHONES);
    store.add(MONITOR);

    store.clear();

    expect(store.isEmpty()).toBe(true);
    expect(store.subtotalCents()).toBe(0);
  });
});
