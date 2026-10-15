import { categoryLabel } from './category-label';

describe('categoryLabel', () => {
  it('translates the known categories', () => {
    expect(categoryLabel('TECHNOLOGY')).toBe('Tecnología');
    expect(categoryLabel('HOME')).toBe('Hogar');
    expect(categoryLabel('CLOTHING')).toBe('Ropa');
  });

  it('shows an unknown category as it arrives', () => {
    expect(categoryLabel('GARDEN')).toBe('GARDEN');
  });
});
