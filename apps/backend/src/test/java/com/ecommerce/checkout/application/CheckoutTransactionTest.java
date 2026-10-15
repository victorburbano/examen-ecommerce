package com.ecommerce.checkout.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ecommerce.catalog.domain.Category;
import com.ecommerce.catalog.domain.Product;
import com.ecommerce.catalog.domain.exception.InsufficientStockException;
import com.ecommerce.catalog.domain.port.ProductRepository;
import com.ecommerce.checkout.domain.Order;
import com.ecommerce.checkout.domain.discount.AppliedCoupon;
import com.ecommerce.checkout.domain.discount.Cart;
import com.ecommerce.checkout.domain.discount.CartLine;
import com.ecommerce.checkout.domain.discount.DiscountBreakdown;
import com.ecommerce.checkout.domain.discount.DiscountStep;
import com.ecommerce.checkout.domain.discount.DiscountStepType;
import com.ecommerce.checkout.domain.port.OrderRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unidad transaccional de la compra: reserva el stock de cada línea y persiste una orden con el
 * desglose congelado. Se verifica la lógica; la atomicidad real la garantiza {@code @Transactional}
 * (probada en los tests de integración).
 */
@ExtendWith(MockitoExtension.class)
class CheckoutTransactionTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneOffset.UTC);

    private static final Product TECH = new Product(1L, "Monitor", Category.TECHNOLOGY, new BigDecimal("80.00"), 10);
    private static final Product HOME = new Product(2L, "Lámpara", Category.HOME, new BigDecimal("50.00"), 5);

    @Mock
    private CheckoutCalculation checkoutCalculation;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    private CheckoutTransaction transaction;

    @BeforeEach
    void setUp() {
        transaction = new CheckoutTransaction(checkoutCalculation, productRepository, orderRepository, CLOCK);
    }

    private static PricedCart priced(Optional<AppliedCoupon> coupon) {
        Cart cart = new Cart(List.of(
                new CartLine(1L, "Monitor", Category.TECHNOLOGY, new BigDecimal("80.00"), 1),
                new CartLine(2L, "Lámpara", Category.HOME, new BigDecimal("50.00"), 1)));
        DiscountBreakdown breakdown = new DiscountBreakdown(
                new BigDecimal("130.00"),
                List.of(new DiscountStep(DiscountStepType.CATEGORY, "Tecnología 10 %", new BigDecimal("10.00"), new BigDecimal("8.00"))),
                new BigDecimal("8.00"),
                new BigDecimal("6.15"),
                new BigDecimal("122.00"),
                false);
        return new PricedCart(cart, List.of(TECH, HOME), coupon, breakdown);
    }

    @Test
    void reservesStockOfEveryLineAndSavesTheOrder() {
        CheckoutCommand command = new CheckoutCommand(
                List.of(new RequestedItem(1L, 1), new RequestedItem(2L, 1)), "WELCOME2026");
        given(checkoutCalculation.price(command))
                .willReturn(priced(Optional.of(new AppliedCoupon("WELCOME2026", new BigDecimal("15.00")))));

        Order order = transaction.place(7L, command);

        assertThat(order.userId()).isEqualTo(7L);
        assertThat(order.couponCode()).isEqualTo("WELCOME2026");
        assertThat(order.createdAt()).isEqualTo(CLOCK.instant());
        assertThat(order.items()).hasSize(2);

        ArgumentCaptor<Product> savedProduct = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, org.mockito.Mockito.times(2)).save(savedProduct.capture());
        assertThat(savedProduct.getAllValues()).extracting(Product::stock).containsExactly(9, 4);
        verify(orderRepository).save(order);
    }

    @Test
    void doesNotTouchTheDatabaseWhenTheStockIsInsufficient() {
        CheckoutCommand command = new CheckoutCommand(List.of(new RequestedItem(1L, 99)), null);
        Cart cart = new Cart(List.of(new CartLine(1L, "Monitor", Category.TECHNOLOGY, new BigDecimal("80.00"), 99)));
        DiscountBreakdown breakdown = new DiscountBreakdown(
                new BigDecimal("7920.00"), List.of(), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("7920.00"), false);
        given(checkoutCalculation.price(command))
                .willReturn(new PricedCart(cart, List.of(TECH), Optional.empty(), breakdown));

        assertThatExceptionOfType(InsufficientStockException.class).isThrownBy(() -> transaction.place(7L, command));
        verify(orderRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
