import { Injectable, computed, effect, inject, signal, untracked } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, finalize, of, take, tap } from 'rxjs';
import { CartStore } from '../cart/cart.store';
import { CheckoutApiService } from './checkout-api.service';
import { isUnauthorized, problemCode, toCheckoutMessage } from './checkout-error';
import { Breakdown, Order, QuoteItem, QuoteRequest } from './checkout.models';

/** Estado de la cotización. `idle` = carrito vacío (no hay checkout que mostrar). */
export type QuoteStatus = 'idle' | 'loading' | 'ready' | 'error';

const QUOTE_FALLBACK = 'No se ha podido calcular el desglose.';
const COUPON_FALLBACK = 'No se ha podido aplicar el cupón.';
const ORDER_FALLBACK = 'No se ha podido completar la compra.';
const LOGIN_RETURN_URL = '/';

/**
 * Estado del checkout. El carrito (spec 002) y el backend (spec 003/004) son las fuentes de
 * verdad: aquí solo se orquesta la re-cotización y la compra.
 *
 * - Re-cotiza al cambiar el carrito (o al vaciarse) mediante un `effect`.
 * - **Descarta respuestas obsoletas** con un contador de secuencia: una respuesta lenta que llega
 *   después de otra petición más reciente se ignora (gana la última cotización).
 * - Un cupón solo se confirma si el backend lo acepta; si falla, se conserva el desglose previo.
 */
@Injectable({ providedIn: 'root' })
export class CheckoutStore {
  private readonly cart = inject(CartStore);
  private readonly api = inject(CheckoutApiService);
  private readonly router = inject(Router);

  private readonly currentQuote = signal<Breakdown | null>(null);
  private readonly quoteStatus = signal<QuoteStatus>('idle');
  private readonly quoteError = signal<string | null>(null);
  private readonly appliedCoupon = signal<string | null>(null);
  private readonly couponErrorSignal = signal<string | null>(null);
  private readonly lastOrderSignal = signal<Order | null>(null);
  private readonly placingOrder = signal(false);
  private readonly stockConflictSignal = signal(false);
  private sequence = 0;

  readonly quote = this.currentQuote.asReadonly();
  readonly status = this.quoteStatus.asReadonly();
  readonly error = this.quoteError.asReadonly();
  readonly coupon = this.appliedCoupon.asReadonly();
  readonly couponError = this.couponErrorSignal.asReadonly();
  readonly lastOrder = this.lastOrderSignal.asReadonly();
  readonly placing = this.placingOrder.asReadonly();
  /** La última compra falló por stock insuficiente (409): la vista debe refrescar el catálogo. */
  readonly stockConflict = this.stockConflictSignal.asReadonly();

  readonly hasQuote = computed(() => this.currentQuote() !== null);
  readonly isQuoting = computed(() => this.quoteStatus() === 'loading');
  /** El banner del tope se deriva de la última cotización y persiste mientras aplique. */
  readonly capApplied = computed(() => this.currentQuote()?.capApplied ?? false);

  private readonly items = computed<readonly QuoteItem[]>(() =>
    this.cart.lines().map((line) => ({ productId: line.product.id, quantity: line.quantity })),
  );

  constructor() {
    effect(() => {
      const items = this.items();
      if (items.length === 0) {
        this.reset();
        return;
      }
      // El cupón se lee `untracked`: confirmarlo no debe disparar una segunda cotización.
      const coupon = untracked(() => this.appliedCoupon());
      this.requestQuote({ items, couponCode: coupon });
    });
  }

  /**
   * Prueba un cupón. Solo se confirma si el backend lo acepta; si falla, el desglose previo y el
   * carrito se conservan y el mensaje se muestra junto al campo.
   */
  applyCoupon(rawCode: string): void {
    const code = rawCode.trim().toUpperCase();
    if (code === '') {
      return;
    }
    const items = this.items();
    if (items.length === 0) {
      return;
    }
    this.couponErrorSignal.set(null);
    this.requestQuote({ items, couponCode: code }, code);
  }

  /**
   * Confirma la compra. Anti doble clic (`placing`), reintenta tras un 401 (el interceptor ya
   * intentó refrescar) y devuelve `null` si algo falla.
   */
  placeOrder(): Observable<Order | null> {
    const items = this.items();
    if (items.length === 0 || this.placingOrder()) {
      return of(null);
    }
    this.placingOrder.set(true);
    this.quoteError.set(null);
    this.stockConflictSignal.set(false);
    const request: QuoteRequest = { items, couponCode: this.appliedCoupon() };
    return this.api.placeOrder(request).pipe(
      tap((order) => this.lastOrderSignal.set(order)),
      catchError((error: unknown) => {
        this.handleOrderError(error);
        return of(null);
      }),
      finalize(() => this.placingOrder.set(false)),
    );
  }

  /** Cierra la confirmación para poder seguir comprando. */
  dismissOrder(): void {
    this.lastOrderSignal.set(null);
  }

  private requestQuote(request: QuoteRequest, candidateCoupon?: string): void {
    const id = ++this.sequence;
    this.quoteStatus.set('loading');
    this.quoteError.set(null);
    this.api
      .quote(request)
      .pipe(take(1))
      .subscribe({
        next: (breakdown) => {
          if (id !== this.sequence) {
            return; // respuesta obsoleta
          }
          this.currentQuote.set(breakdown);
          this.quoteStatus.set('ready');
          if (candidateCoupon !== undefined) {
            this.appliedCoupon.set(candidateCoupon);
            this.couponErrorSignal.set(null);
          }
        },
        error: (error: unknown) => {
          if (id !== this.sequence) {
            return; // respuesta obsoleta
          }
          if (candidateCoupon !== undefined) {
            this.couponErrorSignal.set(toCheckoutMessage(error, COUPON_FALLBACK));
            this.quoteStatus.set(this.currentQuote() !== null ? 'ready' : 'error');
          } else {
            this.currentQuote.set(null);
            this.quoteError.set(toCheckoutMessage(error, QUOTE_FALLBACK));
            this.quoteStatus.set('error');
          }
        },
      });
  }

  private handleOrderError(error: unknown): void {
    if (isUnauthorized(error)) {
      this.quoteError.set('Tu sesión ha caducado. Inicia sesión de nuevo.');
      void this.router.navigate(['/login'], { queryParams: { returnUrl: LOGIN_RETURN_URL } });
      return;
    }
    this.quoteError.set(toCheckoutMessage(error, ORDER_FALLBACK));
    if (problemCode(error) === 'INSUFFICIENT_STOCK') {
      this.stockConflictSignal.set(true);
    }
  }

  private reset(): void {
    this.sequence += 1; // invalida cualquier respuesta en vuelo
    this.currentQuote.set(null);
    this.quoteStatus.set('idle');
    this.quoteError.set(null);
    this.appliedCoupon.set(null);
    this.couponErrorSignal.set(null);
  }
}
