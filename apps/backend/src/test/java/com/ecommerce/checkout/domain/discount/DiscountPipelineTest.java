package com.ecommerce.checkout.domain.discount;

import static org.assertj.core.api.Assertions.assertThat;

import com.ecommerce.catalog.domain.Category;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Matriz del motor con los valores esperados **calculados a mano** (skill `discount-test-matrix`),
 * no derivados del código. Cada fila comprueba además las invariantes del desglose.
 */
class DiscountPipelineTest {

    private static final Category TECH = Category.TECHNOLOGY;
    private static final Category HOME = Category.HOME;

    private static Cart techAndHome() {
        return DiscountFixtures.cartOf(
                DiscountFixtures.line(1L, TECH, "80.00", 1),
                DiscountFixtures.line(2L, HOME, "50.00", 1));
    }

    private static Stream<Arguments> matrix() {
        return Stream.of(
                Arguments.of("no discounts", DiscountFixtures.single(HOME, "50.00"), Optional.empty(),
                        null, null, null, "0.00", "0.00", "50.00", false),
                Arguments.of("category only", DiscountFixtures.single(TECH, "40.00"), Optional.empty(),
                        "4.00", null, null, "4.00", "10.00", "36.00", false),
                Arguments.of("volume exactly at the threshold does not apply",
                        DiscountFixtures.single(HOME, "100.00"), Optional.empty(),
                        null, null, null, "0.00", "0.00", "100.00", false),
                Arguments.of("volume just above the threshold", DiscountFixtures.single(HOME, "100.01"), Optional.empty(),
                        null, "5.00", null, "5.00", "5.00", "95.01", false),
                Arguments.of("coupon only", DiscountFixtures.single(HOME, "100.00"), Optional.of(DiscountFixtures.coupon("15")),
                        null, null, "15.00", "15.00", "15.00", "85.00", false),
                Arguments.of("full cascade", techAndHome(), Optional.of(DiscountFixtures.coupon("15")),
                        "8.00", "6.10", "17.39", "31.49", "24.22", "98.51", false),
                Arguments.of("theoretical maximum (27.33%)", DiscountFixtures.single(TECH, "200.00"),
                        Optional.of(DiscountFixtures.coupon("15")),
                        "20.00", "9.00", "25.65", "54.65", "27.33", "145.35", false),
                Arguments.of("cap exactly reached", DiscountFixtures.single(HOME, "100.00"),
                        Optional.of(DiscountFixtures.coupon("35")),
                        null, null, "35.00", "35.00", "35.00", "65.00", false),
                Arguments.of("cap exceeded", DiscountFixtures.single(TECH, "200.00"),
                        Optional.of(DiscountFixtures.coupon("40")),
                        "20.00", "9.00", "68.40", "70.00", "35.00", "130.00", true),
                Arguments.of("half up on .005 in the volume step", DiscountFixtures.single(HOME, "100.10"), Optional.empty(),
                        null, "5.01", null, "5.01", "5.00", "95.09", false),
                Arguments.of("cap over a non-integer subtotal", DiscountFixtures.single(HOME, "100.01"),
                        Optional.of(DiscountFixtures.coupon("35")),
                        null, "5.00", "33.25", "35.00", "35.00", "65.01", true));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("matrix")
    void appliesTheRulesInCascade(
            String name,
            Cart cart,
            Optional<AppliedCoupon> coupon,
            String categoryAmount,
            String volumeAmount,
            String couponAmount,
            String totalDiscount,
            String effectivePercent,
            String total,
            boolean capApplied) {

        DiscountBreakdown breakdown = DiscountFixtures.pipeline(coupon).calculate(cart);

        assertThat(breakdown.originalSubtotal()).isEqualByComparingTo(cart.subtotal());
        assertStepAmount(breakdown, DiscountStepType.CATEGORY, categoryAmount);
        assertStepAmount(breakdown, DiscountStepType.VOLUME, volumeAmount);
        assertStepAmount(breakdown, DiscountStepType.COUPON, couponAmount);
        assertThat(breakdown.totalDiscount()).isEqualByComparingTo(totalDiscount);
        assertThat(breakdown.effectiveDiscountPercent()).isEqualByComparingTo(effectivePercent);
        assertThat(breakdown.total()).isEqualByComparingTo(total);
        assertThat(breakdown.capApplied()).isEqualTo(capApplied);

        // Invariantes: la suma de pasos es el descuento total y total = original - descuento.
        BigDecimal sum = breakdown.steps().stream()
                .map(DiscountStep::amount)
                .reduce(Money.zero(), BigDecimal::add);
        assertThat(sum).isEqualByComparingTo(breakdown.totalDiscount());
        assertThat(breakdown.originalSubtotal().subtract(breakdown.totalDiscount()))
                .isEqualByComparingTo(breakdown.total());

        if (capApplied) {
            DiscountStep adjustment = stepOf(breakdown, DiscountStepType.CAP_ADJUSTMENT).orElseThrow();
            assertThat(adjustment.amount()).isNegative();
            assertThat(adjustment.percent()).isNull();
        } else {
            assertThat(stepOf(breakdown, DiscountStepType.CAP_ADJUSTMENT)).isEmpty();
        }
    }

    @Test
    void keepsTheStepsInPipelineOrder() {
        DiscountBreakdown breakdown = DiscountFixtures.pipeline(Optional.of(DiscountFixtures.coupon("15")))
                .calculate(techAndHome());

        assertThat(breakdown.steps().stream().map(DiscountStep::type))
                .containsExactly(DiscountStepType.CATEGORY, DiscountStepType.VOLUME, DiscountStepType.COUPON);
    }

    @Test
    void theCascadeIsNotTheSumOfThePercentages() {
        DiscountBreakdown breakdown = DiscountFixtures.pipeline(Optional.of(DiscountFixtures.coupon("15")))
                .calculate(techAndHome());

        // 10 % + 5 % + 15 % = 30 % en suma simple, pero en cascada el efectivo es 24.22 %.
        assertThat(breakdown.effectiveDiscountPercent()).isEqualByComparingTo("24.22");
        assertThat(breakdown.effectiveDiscountPercent()).isNotEqualByComparingTo("30.00");
    }

    @Test
    void theOrderOfTheRulesMatters() {
        DiscountProperties properties = DiscountFixtures.defaults();
        Cart cart = techAndHome();

        DiscountBreakdown inOrder = new DiscountPipeline(
                List.of(new CategoryDiscountRule(properties), new VolumeDiscountRule(properties)),
                new DiscountCapPolicy(properties),
                Optional.empty()).calculate(cart);
        DiscountBreakdown permuted = new DiscountPipeline(
                List.of(new VolumeDiscountRule(properties), new CategoryDiscountRule(properties)),
                new DiscountCapPolicy(properties),
                Optional.empty()).calculate(cart);

        assertThat(inOrder.totalDiscount()).isEqualByComparingTo("14.10");
        assertThat(permuted.totalDiscount()).isEqualByComparingTo("14.50");
        assertThat(inOrder.totalDiscount()).isNotEqualByComparingTo(permuted.totalDiscount());
    }

    @Test
    void appliesAConfigurableCap() {
        DiscountPipelineFactory factory =
                new DiscountPipelineFactory(DiscountFixtures.with("10", "100.00", "5", "10"));

        // Si el tope es 10 %: cat 10.00 (100 → 90), volumen no (90 no supera 100),
        // cupón 15 % de 90 = 13.50 (pre-tope 23.50) → ajuste -13.50.
        DiscountBreakdown breakdown = factory.create(Optional.of(DiscountFixtures.coupon("15")))
                .calculate(DiscountFixtures.single(TECH, "100.00"));

        assertThat(stepOf(breakdown, DiscountStepType.CATEGORY).orElseThrow().amount()).isEqualByComparingTo("10.00");
        assertThat(stepOf(breakdown, DiscountStepType.COUPON).orElseThrow().amount()).isEqualByComparingTo("13.50");
        assertThat(breakdown.totalDiscount()).isEqualByComparingTo("10.00");
        assertThat(breakdown.effectiveDiscountPercent()).isEqualByComparingTo("10.00");
        assertThat(breakdown.total()).isEqualByComparingTo("90.00");
        assertThat(breakdown.capApplied()).isTrue();
    }

    @Test
    void handlesAZeroSubtotal() {
        DiscountBreakdown breakdown = DiscountFixtures.pipeline().calculate(DiscountFixtures.single(HOME, "0.00"));

        assertThat(breakdown.originalSubtotal()).isEqualByComparingTo("0.00");
        assertThat(breakdown.totalDiscount()).isEqualByComparingTo("0.00");
        assertThat(breakdown.effectiveDiscountPercent()).isEqualByComparingTo("0.00");
        assertThat(breakdown.total()).isEqualByComparingTo("0.00");
        assertThat(breakdown.capApplied()).isFalse();
    }

    private static Optional<DiscountStep> stepOf(DiscountBreakdown breakdown, DiscountStepType type) {
        return breakdown.steps().stream().filter(step -> step.type() == type).findFirst();
    }

    private static void assertStepAmount(DiscountBreakdown breakdown, DiscountStepType type, String expected) {
        Optional<DiscountStep> step = stepOf(breakdown, type);
        if (expected == null) {
            assertThat(step).as("no esperaba paso %s", type).isEmpty();
        } else {
            assertThat(step).as("esperaba paso %s", type).isPresent();
            assertThat(step.orElseThrow().amount()).isEqualByComparingTo(expected);
        }
    }
}
