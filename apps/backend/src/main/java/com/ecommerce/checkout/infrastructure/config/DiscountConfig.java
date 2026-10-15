package com.ecommerce.checkout.infrastructure.config;

import com.ecommerce.checkout.domain.discount.DiscountPipelineFactory;
import com.ecommerce.checkout.domain.discount.DiscountProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cableado del motor de descuentos (excluido de cobertura): convierte {@code app.discounts.*} en
 * el value object de dominio y expone la factoría de pipelines.
 */
@Configuration
@EnableConfigurationProperties(DiscountSettings.class)
public class DiscountConfig {

    @Bean
    public DiscountProperties discountProperties(DiscountSettings settings) {
        return new DiscountProperties(
                settings.category(),
                settings.categoryPercent(),
                settings.volumeThreshold(),
                settings.volumePercent(),
                settings.maxTotalPercent());
    }

    @Bean
    public DiscountPipelineFactory discountPipelineFactory(DiscountProperties discountProperties) {
        return new DiscountPipelineFactory(discountProperties);
    }
}
