package com.ecommerce.checkout.domain.discount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ecommerce.catalog.domain.Category;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DiscountPropertiesTest {

    @Test
    void acceptsTheDefaultsAndNormalizesTheMoneyFields() {
        DiscountProperties properties = DiscountFixtures.defaults();

        assertThat(properties.category()).isEqualTo(Category.TECHNOLOGY);
        assertThat(properties.categoryPercent()).isEqualByComparingTo("10.00");
        assertThat(properties.volumeThreshold()).isEqualByComparingTo("100.00");
        assertThat(properties.volumePercent()).isEqualByComparingTo("5.00");
        assertThat(properties.maxTotalPercent()).isEqualByComparingTo("35.00");
    }

    @Test
    void rejectsANullCategory() {
        assertThatThrownBy(() -> new DiscountProperties(
                        null, new BigDecimal("10"), new BigDecimal("100.00"), new BigDecimal("5"), new BigDecimal("35")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("category");
    }

    @ParameterizedTest
    @CsvSource({
        "category-percent, null",
        "category-percent, 0",
        "category-percent, -1",
        "category-percent, 100.01",
        "volume-percent, 0",
        "volume-percent, 101",
        "max-total-percent, 0",
        "max-total-percent, 101",
    })
    void rejectsPercentsOutsideTheRange(String property, String value) {
        String categoryPercent = "10";
        String volumePercent = "5";
        String maxTotalPercent = "35";
        switch (property) {
            case "category-percent" -> categoryPercent = value;
            case "volume-percent" -> volumePercent = value;
            default -> maxTotalPercent = value;
        }
        BigDecimal category = "null".equals(categoryPercent) ? null : new BigDecimal(categoryPercent);
        BigDecimal volume = "null".equals(volumePercent) ? null : new BigDecimal(volumePercent);
        BigDecimal max = "null".equals(maxTotalPercent) ? null : new BigDecimal(maxTotalPercent);
        BigDecimal threshold = new BigDecimal("100.00");

        assertThatThrownBy(() -> new DiscountProperties(Category.TECHNOLOGY, category, threshold, volume, max))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(property);
    }

    @Test
    void rejectsANullOrNegativeVolumeThreshold() {
        assertThatThrownBy(() -> DiscountFixtures.with("10", "-1", "5", "35"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("volume-threshold");
        assertThatThrownBy(() -> new DiscountProperties(
                        Category.TECHNOLOGY, new BigDecimal("10"), null, new BigDecimal("5"), new BigDecimal("35")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("volume-threshold");
    }

    @Test
    void acceptsAZeroVolumeThreshold() {
        assertThat(DiscountFixtures.with("10", "0", "5", "35").volumeThreshold())
                .isEqualByComparingTo("0.00");
    }
}
