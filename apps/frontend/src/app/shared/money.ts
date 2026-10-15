/**
 * Utilidades de dinero. La fuente de verdad son los **céntimos enteros**: nunca se acumulan
 * importes con `number` de coma flotante. Los importes del API llegan como string decimal exacto
 * (p. ej. `"59.90"`).
 */

const MONEY_PATTERN = /^(?<units>\d+)(?:\.(?<fraction>\d{1,2}))?$/;

/** Convierte un importe decimal exacto (`"59.90"`) a céntimos enteros. */
export function toCents(price: string): number {
  const match = MONEY_PATTERN.exec(price.trim());
  const units = match?.groups?.['units'];
  if (units === undefined) {
    throw new Error(`Invalid money amount: ${price}`);
  }
  const fraction = (match?.groups?.['fraction'] ?? '').padEnd(2, '0');
  return Number(units) * 100 + Number(fraction);
}

/**
 * Solo para **mostrar**: pasa céntimos a unidades monetarias para el `CurrencyPipe`.
 * No usar el resultado para calcular.
 */
export function centsToAmount(cents: number): number {
  return cents / 100;
}
