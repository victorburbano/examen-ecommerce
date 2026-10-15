import { centsToAmount, toCents } from './money';

describe('money', () => {
  describe('toCents', () => {
    it('converts exact decimal prices to integer cents', () => {
      expect(toCents('59.90')).toBe(5990);
      expect(toCents('249.00')).toBe(24900);
      expect(toCents('9.99')).toBe(999);
      expect(toCents('0.05')).toBe(5);
    });

    it('accepts integers and one-decimal amounts', () => {
      expect(toCents('24')).toBe(2400);
      expect(toCents('19.9')).toBe(1990);
      expect(toCents('0')).toBe(0);
    });

    it('ignores surrounding whitespace', () => {
      expect(toCents('  19.90  ')).toBe(1990);
    });

    it('adds prices without floating point drift', () => {
      // 0.10 + 0.20 === 0.30 en céntimos; con floats daría 0.30000000000000004.
      expect(toCents('0.10') + toCents('0.20')).toBe(toCents('0.30'));
    });

    it('rejects amounts that are not plain decimals', () => {
      expect(() => toCents('abc')).toThrow('Invalid money amount');
      expect(() => toCents('19.999')).toThrow('Invalid money amount');
      expect(() => toCents('-1.00')).toThrow('Invalid money amount');
      expect(() => toCents('')).toThrow('Invalid money amount');
      expect(() => toCents('1,50')).toThrow('Invalid money amount');
    });
  });

  describe('centsToAmount', () => {
    it('converts cents to the display amount', () => {
      expect(centsToAmount(5990)).toBeCloseTo(59.9, 10);
      expect(centsToAmount(0)).toBe(0);
    });
  });
});
