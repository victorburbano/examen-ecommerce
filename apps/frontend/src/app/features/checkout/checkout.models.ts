/**
 * Modelos del checkout: reflejan los DTOs de la spec 004. Todos de solo lectura y con los
 * importes como **string decimal exacto** (p. ej. `"98.51"`): el front nunca recalcula el desglose,
 * solo lo muestra. El backend es la única fuente de cálculo.
 */

/** Tipos de línea del desglose (orden real de la cascada del motor). */
export type DiscountStepType = 'CATEGORY' | 'VOLUME' | 'COUPON' | 'CAP_ADJUSTMENT';

/** Línea del desglose tal como la devuelve la API. */
export interface ItemLine {
  readonly productId: number;
  readonly name: string;
  readonly category: string;
  readonly unitPrice: string;
  readonly quantity: number;
  readonly lineTotal: string;
}

/** Paso de descuento. `percent` es `null` en el ajuste por tope (`CAP_ADJUSTMENT`). */
export interface DiscountLine {
  readonly type: DiscountStepType;
  readonly label: string;
  readonly percent: string | null;
  readonly amount: string;
}

/** Desglose completo de una cotización (`POST /api/checkout/quote`). */
export interface Breakdown {
  readonly items: readonly ItemLine[];
  readonly discounts: readonly DiscountLine[];
  readonly originalSubtotal: string;
  readonly totalDiscount: string;
  readonly effectiveDiscountPercent: string;
  readonly total: string;
  readonly capApplied: boolean;
  readonly couponCode: string | null;
}

/** Orden confirmada (`POST /api/checkout/orders`): el desglose más identificador y fecha. */
export interface Order extends Breakdown {
  readonly orderId: string;
  readonly createdAt: string;
}

/** Página de la API (`GET /api/orders`). */
export interface PageResult<T> {
  readonly content: readonly T[];
  readonly page: number;
  readonly size: number;
  readonly totalElements: number;
  readonly totalPages: number;
}

/** Ítem del carrito enviado al backend: solo producto y cantidad (los precios los pone el servidor). */
export interface QuoteItem {
  readonly productId: number;
  readonly quantity: number;
}

/** Cuerpo común de `quote` y `placeOrder`. */
export interface QuoteRequest {
  readonly items: readonly QuoteItem[];
  readonly couponCode: string | null;
}
