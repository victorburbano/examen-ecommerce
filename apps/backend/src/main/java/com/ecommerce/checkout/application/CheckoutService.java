package com.ecommerce.checkout.application;

import com.ecommerce.checkout.domain.Order;
import com.ecommerce.checkout.domain.exception.StockConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

/**
 * Orquesta la compra con reintento acotado ante conflictos de stock. El reintento tiene que
 * re-ejecutar la transacción **completa**, así que el bucle vive aquí (sin {@code @Transactional})
 * y cada intento es una transacción nueva en {@link CheckoutTransaction}: una transacción que ya
 * ha fallado por locking no se puede reintentar dentro de sí misma.
 */
@Service
public class CheckoutService {

    private static final int MAX_ATTEMPTS = 3;

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);

    private final CheckoutTransaction checkoutTransaction;

    public CheckoutService(CheckoutTransaction checkoutTransaction) {
        this.checkoutTransaction = checkoutTransaction;
    }

    public Order checkout(Long userId, CheckoutCommand command) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return checkoutTransaction.place(userId, command);
            } catch (OptimisticLockingFailureException ex) {
                log.warn("Stock conflict while placing an order (attempt {}/{})", attempt, MAX_ATTEMPTS);
            }
        }
        throw new StockConflictException();
    }
}
