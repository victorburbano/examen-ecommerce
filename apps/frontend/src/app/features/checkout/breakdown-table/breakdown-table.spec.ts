import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BreakdownTable } from './breakdown-table';
import { Breakdown } from '../checkout.models';

const WITH_CAP: Breakdown = {
  items: [
    {
      productId: 1,
      name: 'Monitor 27"',
      category: 'TECHNOLOGY',
      unitPrice: '80.00',
      quantity: 1,
      lineTotal: '80.00',
    },
    {
      productId: 2,
      name: 'Lámpara',
      category: 'HOME',
      unitPrice: '50.00',
      quantity: 1,
      lineTotal: '50.00',
    },
  ],
  discounts: [
    { type: 'CATEGORY', label: 'Tecnología 10 %', percent: '10.00', amount: '8.00' },
    { type: 'VOLUME', label: 'Volumen 5 %', percent: '5.00', amount: '6.10' },
    { type: 'COUPON', label: 'Cupón WELCOME2026 15 %', percent: '15.00', amount: '17.39' },
    { type: 'CAP_ADJUSTMENT', label: 'Ajuste por tope', percent: null, amount: '-4.55' },
  ],
  originalSubtotal: '130.00',
  totalDiscount: '45.50',
  effectiveDiscountPercent: '35.00',
  total: '84.50',
  capApplied: true,
  couponCode: 'WELCOME2026',
};

describe('BreakdownTable', () => {
  async function create(data: Breakdown): Promise<ComponentFixture<BreakdownTable>> {
    TestBed.configureTestingModule({ imports: [BreakdownTable] });
    const fixture = TestBed.createComponent(BreakdownTable);
    fixture.componentRef.setInput('breakdown', data);
    await fixture.whenStable();
    return fixture;
  }

  it('renders the items with translated categories', async () => {
    const fixture = await create(WITH_CAP);
    const content: string = fixture.nativeElement.textContent;

    expect(content).toContain('Monitor 27"');
    expect(content).toContain('Tecnología');
    expect(content).toContain('Lámpara');
    expect(content).toContain('Hogar');
  });

  it('renders every discount line, the effective percentage, savings and total', async () => {
    const fixture = await create(WITH_CAP);
    const content: string = fixture.nativeElement.textContent;

    expect(content).toContain('Tecnología 10 %');
    expect(content).toContain('Volumen 5 %');
    expect(content).toContain('Cupón WELCOME2026 15 %');
    expect(content).toContain('Ajuste por tope');
    expect(content).toContain('130.00'); // subtotal
    expect(content).toContain('45.50'); // savings
    expect(content).toContain('35.00'); // effective %
    expect(content).toContain('84.50'); // total
  });

  it('omits the percent on the cap adjustment line', async () => {
    const fixture = await create(WITH_CAP);

    const capRow = fixture.nativeElement.querySelector('tr.discount--cap') as HTMLElement;
    expect(capRow).not.toBeNull();
    expect(capRow.querySelector('.pct')).toBeNull();
    expect(capRow.textContent).toContain('Ajuste por tope');
  });
});
