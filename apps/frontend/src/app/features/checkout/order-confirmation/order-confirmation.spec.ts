import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { OrderConfirmation } from './order-confirmation';
import { Order } from '../checkout.models';

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
  discounts: [{ type: 'CATEGORY', label: 'Tecnología 10 %', percent: '10.00', amount: '8.00' }],
  originalSubtotal: '130.00',
  totalDiscount: '31.49',
  effectiveDiscountPercent: '24.22',
  total: '98.51',
  capApplied: false,
  couponCode: 'WELCOME2026',
};

describe('OrderConfirmation', () => {
  async function create(order: Order): Promise<ComponentFixture<OrderConfirmation>> {
    TestBed.configureTestingModule({ imports: [OrderConfirmation], providers: [provideRouter([])] });
    const fixture = TestBed.createComponent(OrderConfirmation);
    fixture.componentRef.setInput('order', order);
    await fixture.whenStable();
    return fixture;
  }

  it('shows the order id and totals', async () => {
    const fixture = await create(ORDER);
    const content: string = fixture.nativeElement.textContent;

    expect(content).toContain('55555555-5555-5555-5555-555555555555');
    expect(content).toContain('130.00');
    expect(content).toContain('31.49');
    expect(content).toContain('98.51');
    expect(content).toContain('24.22');
  });

  it('does not mention the cap when it did not apply', async () => {
    const fixture = await create(ORDER);

    expect(fixture.nativeElement.textContent).not.toContain('tope máximo');
  });

  it('mentions the cap when it applied', async () => {
    const fixture = await create({ ...ORDER, capApplied: true });

    expect(fixture.nativeElement.textContent).toContain('tope máximo');
  });

  it('emits dismiss when clicking "Seguir comprando"', async () => {
    const fixture = await create(ORDER);
    const dismissed = jest.fn();
    fixture.componentInstance.dismiss.subscribe(dismissed);

    const buttons = Array.from(
      fixture.nativeElement.querySelectorAll('button'),
    ) as HTMLButtonElement[];
    const dismissButton = buttons.find((button) => button.textContent?.includes('Seguir comprando'));
    dismissButton?.click();

    expect(dismissed).toHaveBeenCalled();
  });
});
