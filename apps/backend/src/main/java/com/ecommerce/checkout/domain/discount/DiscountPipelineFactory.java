package com.ecommerce.checkout.domain.discount;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Factory: arma el pipeline por petición. El orden es fijo (categoría → volumen → cupón) y la
 * regla de cupón **solo entra si hay cupón**; el tope siempre va al final.
 */
public final class DiscountPipelineFactory {

    private final DiscountProperties properties;

    public DiscountPipelineFactory(DiscountProperties properties) {
        this.properties = properties;
    }

    public DiscountPipeline create(Optional<AppliedCoupon> coupon) {
        List<DiscountRule> rules = new ArrayList<>();
        rules.add(new CategoryDiscountRule(properties));
        rules.add(new VolumeDiscountRule(properties));
        coupon.ifPresent(applied -> rules.add(new CouponDiscountRule()));
        return new DiscountPipeline(rules, new DiscountCapPolicy(properties), coupon);
    }
}
