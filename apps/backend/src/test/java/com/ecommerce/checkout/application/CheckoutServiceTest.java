package com.ecommerce.checkout.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ecommerce.checkout.domain.Order;
import com.ecommerce.checkout.domain.exception.StockConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

/**
 * Reintento acotado ante conflictos de stock: reintenta hasta 3 veces y, si no lo logra,
 * responde con {@link StockConflictException} (409).
 */
@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private CheckoutTransaction checkoutTransaction;

    @InjectMocks
    private CheckoutService checkoutService;

    private final CheckoutCommand command = new CheckoutCommand(java.util.List.of(new RequestedItem(1L, 1)), null);

    @Test
    void returnsTheOrderWhenTheFirstAttemptSucceeds() {
        Order order = org.mockito.Mockito.mock(Order.class);
        given(checkoutTransaction.place(5L, command)).willReturn(order);

        assertThat(checkoutService.checkout(5L, command)).isEqualTo(order);
        verify(checkoutTransaction, times(1)).place(5L, command);
    }

    @Test
    void retriesUntilTheTransactionSucceeds() {
        Order order = org.mockito.Mockito.mock(Order.class);
        given(checkoutTransaction.place(5L, command))
                .willThrow(new OptimisticLockingFailureException("conflict"))
                .willThrow(new OptimisticLockingFailureException("conflict"))
                .willReturn(order);

        assertThat(checkoutService.checkout(5L, command)).isEqualTo(order);
        verify(checkoutTransaction, times(3)).place(5L, command);
    }

    @Test
    void givesUpAfterThreeAttempts() {
        given(checkoutTransaction.place(5L, command)).willThrow(new OptimisticLockingFailureException("conflict"));

        assertThatExceptionOfType(StockConflictException.class).isThrownBy(() -> checkoutService.checkout(5L, command));
        verify(checkoutTransaction, times(3)).place(5L, command);
    }
}
