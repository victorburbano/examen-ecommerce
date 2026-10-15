package com.ecommerce.checkout.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cotización pública (sin efectos secundarios): delega el cálculo en {@link CheckoutCalculation}. */
@Service
public class QuoteService {

    private final CheckoutCalculation checkoutCalculation;

    public QuoteService(CheckoutCalculation checkoutCalculation) {
        this.checkoutCalculation = checkoutCalculation;
    }

    @Transactional(readOnly = true)
    public PricedCart quote(CheckoutCommand command) {
        return checkoutCalculation.price(command);
    }
}
