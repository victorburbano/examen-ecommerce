package com.ecommerce.checkout.domain.discount;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Pipeline ordenado de reglas. Ejecuta cada regla con el total corriente, acumula el desglose y al
 * final aplica el tope. Es inmutable y se construye por petición con {@link DiscountPipelineFactory}.
 */
public final class DiscountPipeline {

    private final List<DiscountRule> rules;
    private final DiscountCapPolicy capPolicy;
    private final Optional<AppliedCoupon> coupon;

    public DiscountPipeline(
            List<DiscountRule> rules, DiscountCapPolicy capPolicy, Optional<AppliedCoupon> coupon) {
        this.rules = List.copyOf(rules);
        this.capPolicy = capPolicy;
        this.coupon = coupon;
    }

    public DiscountBreakdown calculate(Cart cart) {
        BigDecimal originalSubtotal = cart.subtotal();
        DiscountContext context = new DiscountContext(cart, originalSubtotal, originalSubtotal, coupon);
        List<DiscountStep> steps = new ArrayList<>();

        for (DiscountRule rule : rules) {
            Optional<DiscountStep> step = rule.apply(context);
            if (step.isPresent()) {
                steps.add(step.get());
                context = context.withRunningTotal(context.runningTotal().subtract(step.get().amount()));
            }
        }

        BigDecimal totalDiscount = totalOf(steps);
        Optional<DiscountStep> adjustment = capPolicy.apply(originalSubtotal, totalDiscount);
        if (adjustment.isPresent()) {
            steps.add(adjustment.get());
            totalDiscount = totalDiscount.add(adjustment.get().amount());
        }

        return new DiscountBreakdown(
                originalSubtotal,
                steps,
                totalDiscount,
                Money.ratioAsPercent(totalDiscount, originalSubtotal),
                originalSubtotal.subtract(totalDiscount),
                adjustment.isPresent());
    }

    private static BigDecimal totalOf(List<DiscountStep> steps) {
        return steps.stream().map(DiscountStep::amount).reduce(Money.zero(), BigDecimal::add);
    }
}
