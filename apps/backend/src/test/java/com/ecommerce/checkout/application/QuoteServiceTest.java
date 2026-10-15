package com.ecommerce.checkout.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ecommerce.catalog.domain.Category;
import com.ecommerce.catalog.domain.Product;
import com.ecommerce.checkout.domain.discount.Cart;
import com.ecommerce.checkout.domain.discount.CartLine;
import com.ecommerce.checkout.domain.discount.DiscountBreakdown;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** La cotización delega el cálculo y no tiene efectos secundarios. */
@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    @Mock
    private CheckoutCalculation checkoutCalculation;

    @InjectMocks
    private QuoteService quoteService;

    @Test
    void delegatesTheCalculation() {
        PricedCart priced = new PricedCart(
                new Cart(List.of(new CartLine(1L, "Monitor", Category.TECHNOLOGY, new BigDecimal("80.00"), 1))),
                List.of(new Product(1L, "Monitor", Category.TECHNOLOGY, new BigDecimal("80.00"), 10)),
                Optional.empty(),
                new DiscountBreakdown(
                        new BigDecimal("80.00"), List.of(), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("80.00"), false));
        CheckoutCommand command = new CheckoutCommand(List.of(new RequestedItem(1L, 1)), null);
        given(checkoutCalculation.price(command)).willReturn(priced);

        assertThat(quoteService.quote(command)).isEqualTo(priced);
        verify(checkoutCalculation).price(command);
    }
}
