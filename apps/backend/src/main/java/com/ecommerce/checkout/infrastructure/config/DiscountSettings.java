package com.ecommerce.checkout.infrastructure.config;

import com.ecommerce.catalog.domain.Category;
import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Enlace crudo de {@code app.discounts.*}. Vive en infraestructura para que el dominio
 * ({@code DiscountProperties}) no dependa de Spring. La validación de rangos la hace el dominio.
 */
@ConfigurationProperties(prefix = "app.discounts")
public record DiscountSettings(
        Category category,
        BigDecimal categoryPercent,
        BigDecimal volumeThreshold,
        BigDecimal volumePercent,
        BigDecimal maxTotalPercent) {
}
