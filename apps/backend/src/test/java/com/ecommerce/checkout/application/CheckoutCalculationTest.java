package com.ecommerce.checkout.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;

import com.ecommerce.catalog.domain.Category;
import com.ecommerce.catalog.domain.Product;
import com.ecommerce.catalog.domain.exception.ProductNotFoundException;
import com.ecommerce.catalog.domain.port.ProductRepository;
import com.ecommerce.checkout.domain.Coupon;
import com.ecommerce.checkout.domain.discount.DiscountBreakdown;
import com.ecommerce.checkout.domain.discount.DiscountPipelineFactory;
import com.ecommerce.checkout.domain.discount.DiscountProperties;
import com.ecommerce.checkout.domain.discount.DiscountStepType;
import com.ecommerce.checkout.domain.discount.exception.EmptyCartException;
import com.ecommerce.checkout.domain.discount.exception.InvalidCartException;
import com.ecommerce.checkout.domain.exception.CouponExpiredException;
import com.ecommerce.checkout.domain.exception.CouponNotFoundException;
import com.ecommerce.checkout.domain.port.CouponRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Cálculo compartido de quote/orders: reconstruye el carrito desde la BD, resuelve el cupón y
 * aplica el motor. Usa un {@link Clock} fijo (2026-06-15) para que la vigencia sea determinista.
 */
@ExtendWith(MockitoExtension.class)
class CheckoutCalculationTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 15);

    private static final Product TECH_80 = new Product(1L, "Monitor 27\"", Category.TECHNOLOGY, new BigDecimal("80.00"), 10);
    private static final Product HOME_50 = new Product(2L, "Lámpara", Category.HOME, new BigDecimal("50.00"), 5);

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CouponRepository couponRepository;

    private CheckoutCalculation calculation;

    @BeforeEach
    void setUp() {
        DiscountProperties properties = new DiscountProperties(
                Category.TECHNOLOGY, new BigDecimal("10"), new BigDecimal("100.00"), new BigDecimal("5"), new BigDecimal("35"));
        calculation = new CheckoutCalculation(
                productRepository, couponRepository, new DiscountPipelineFactory(properties), CLOCK);
    }

    @Test
    void pricesTheCartAndAppliesTheCoupon() {
        given(productRepository.findAllById(List.of(1L, 2L))).willReturn(List.of(TECH_80, HOME_50));
        given(couponRepository.findByCode("WELCOME2026"))
                .willReturn(Optional.of(new Coupon("WELCOME2026", new BigDecimal("15.00"), TODAY.minusDays(10), TODAY.plusDays(10))));

        PricedCart priced = calculation.price(new CheckoutCommand(
                List.of(new RequestedItem(1L, 1), new RequestedItem(2L, 1)), "WELCOME2026"));

        DiscountBreakdown breakdown = priced.breakdown();
        assertThat(breakdown.originalSubtotal()).isEqualByComparingTo("130.00");
        assertThat(breakdown.steps()).extracting(s -> s.type())
                .containsExactly(DiscountStepType.CATEGORY, DiscountStepType.VOLUME, DiscountStepType.COUPON);
        assertThat(breakdown.steps()).extracting(s -> s.amount())
                .containsExactly(new BigDecimal("8.00"), new BigDecimal("6.10"), new BigDecimal("17.39"));
        assertThat(breakdown.totalDiscount()).isEqualByComparingTo("31.49");
        assertThat(breakdown.total()).isEqualByComparingTo("98.51");
        assertThat(breakdown.effectiveDiscountPercent()).isEqualByComparingTo("24.22");
        assertThat(breakdown.capApplied()).isFalse();
        assertThat(priced.coupon()).map(c -> c.code()).contains("WELCOME2026");
        assertThat(priced.products()).containsExactly(TECH_80, HOME_50);
    }

    @Test
    void appliesTheCapWhenTheTotalDiscountExceedsThePolicy() {
        Product tech249 = new Product(1L, "Monitor 27\"", Category.TECHNOLOGY, new BigDecimal("249.00"), 10);
        given(productRepository.findAllById(List.of(1L))).willReturn(List.of(tech249));
        given(couponRepository.findByCode("MEGA40"))
                .willReturn(Optional.of(new Coupon("MEGA40", new BigDecimal("40.00"), TODAY.minusDays(10), TODAY.plusDays(10))));

        PricedCart priced = calculation.price(new CheckoutCommand(List.of(new RequestedItem(1L, 1)), "MEGA40"));

        DiscountBreakdown breakdown = priced.breakdown();
        assertThat(breakdown.capApplied()).isTrue();
        assertThat(breakdown.effectiveDiscountPercent()).isEqualByComparingTo("35.00");
        assertThat(breakdown.total()).isEqualByComparingTo("161.85");
        assertThat(breakdown.steps().get(breakdown.steps().size() - 1).type())
                .isEqualTo(DiscountStepType.CAP_ADJUSTMENT);
    }

    @Test
    void quotesWithoutCouponWhenTheCodeIsNullOrEmpty() {
        given(productRepository.findAllById(List.of(1L))).willReturn(List.of(TECH_80));

        assertThat(calculation.price(new CheckoutCommand(List.of(new RequestedItem(1L, 1)), null)).coupon())
                .isEmpty();
        assertThat(calculation.price(new CheckoutCommand(List.of(new RequestedItem(1L, 1)), "   ")).coupon())
                .isEmpty();
    }

    @Test
    void normalizesTheCouponCodeBeforeLookingItUp() {
        given(productRepository.findAllById(List.of(1L))).willReturn(List.of(TECH_80));
        given(couponRepository.findByCode("WELCOME2026"))
                .willReturn(Optional.of(new Coupon("WELCOME2026", new BigDecimal("15.00"), TODAY.minusDays(1), TODAY.plusDays(1))));

        PricedCart priced = calculation.price(new CheckoutCommand(List.of(new RequestedItem(1L, 1)), "  welcome2026 "));

        assertThat(priced.coupon()).map(c -> c.code()).contains("WELCOME2026");
    }

    @Test
    void rejectsAnEmptyCart() {
        assertThatExceptionOfType(EmptyCartException.class)
                .isThrownBy(() -> calculation.price(new CheckoutCommand(List.of(), null)));
        assertThatExceptionOfType(EmptyCartException.class)
                .isThrownBy(() -> calculation.price(new CheckoutCommand(null, null)));
    }

    @Test
    void rejectsADuplicatedProductId() {
        assertThatExceptionOfType(InvalidCartException.class)
                .isThrownBy(() -> calculation.price(new CheckoutCommand(
                        List.of(new RequestedItem(1L, 1), new RequestedItem(1L, 2)), null)))
                .withMessageContaining("duplicate");
    }

    @Test
    void rejectsAnUnknownProduct() {
        given(productRepository.findAllById(List.of(99L))).willReturn(List.of());

        assertThatExceptionOfType(ProductNotFoundException.class)
                .isThrownBy(() -> calculation.price(new CheckoutCommand(List.of(new RequestedItem(99L, 1)), null)));
    }

    @Test
    void rejectsAnUnknownCoupon() {
        given(productRepository.findAllById(List.of(1L))).willReturn(List.of(TECH_80));
        given(couponRepository.findByCode("NOPE")).willReturn(Optional.empty());

        assertThatExceptionOfType(CouponNotFoundException.class)
                .isThrownBy(() -> calculation.price(new CheckoutCommand(List.of(new RequestedItem(1L, 1)), "NOPE")));
    }

    @Test
    void rejectsAnExpiredCoupon() {
        given(productRepository.findAllById(List.of(1L))).willReturn(List.of(TECH_80));
        given(couponRepository.findByCode("EXPIRED2025"))
                .willReturn(Optional.of(new Coupon("EXPIRED2025", new BigDecimal("10.00"), TODAY.minusYears(1), TODAY.minusDays(1))));

        assertThatExceptionOfType(CouponExpiredException.class)
                .isThrownBy(() -> calculation.price(new CheckoutCommand(List.of(new RequestedItem(1L, 1)), "EXPIRED2025")));
    }
}
