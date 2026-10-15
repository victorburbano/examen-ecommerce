/**
 * Producto del catálogo, tal como lo devuelve `GET /api/products`.
 * El precio es un string decimal exacto (p. ej. `"59.90"`): no se convierte a `number`.
 */
export interface Product {
  readonly id: number;
  readonly name: string;
  readonly price: string;
  readonly category: string;
  readonly stock: number;
}
