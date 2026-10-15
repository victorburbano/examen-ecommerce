package com.ecommerce.checkout.domain.discount;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MoneyTest {

    @Test
    void zeroIsScaleTwo() {
        assertThat(Money.zero()).isEqualByComparingTo("0.00");
        assertThat(Money.zero().scale()).isEqualTo(Money.SCALE);
    }

    @ParameterizedTest
    @CsvSource({
        "27.325, 27.33",
        "5.0005, 5.00",
        "24.2230, 24.22",
        "0.005, 0.01",
        "0.004, 0.00",
        "8, 8.00",
        "59.90, 59.90",
    })
    void roundsHalfUpToTwoDecimals(String input, String expected) {
        assertThat(Money.round(new BigDecimal(input))).isEqualByComparingTo(expected);
        assertThat(Money.round(new BigDecimal(input)).scale()).isEqualTo(Money.SCALE);
    }

    @ParameterizedTest
    @CsvSource({
        "80.00, 10, 8.00",
        "100.01, 5, 5.00",
        "100.10, 5, 5.01",
        "115.90, 15, 17.39",
        "171.00, 40, 68.40",
        "0.05, 10, 0.01",
        "200.00, 35, 70.00",
    })
    void computesAPercentageOfAnAmount(String amount, String percent, String expected) {
        assertThat(Money.percentOf(new BigDecimal(amount), new BigDecimal(percent)))
                .isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "31.49, 130.00, 24.22",
        "54.65, 200.00, 27.33",
        "35.00, 100.00, 35.00",
        "5.01, 100.10, 5.00",
        "14.10, 130.00, 10.85",
        "0.00, 130.00, 0.00",
    })
    void expressesARatioAsAPercent(String part, String whole, String expected) {
        assertThat(Money.ratioAsPercent(new BigDecimal(part), new BigDecimal(whole)))
                .isEqualByComparingTo(expected);
    }

    @Test
    void ratioWithAZeroWholeIsZero() {
        assertThat(Money.ratioAsPercent(new BigDecimal("10.00"), BigDecimal.ZERO))
                .isEqualByComparingTo("0.00");
    }
}
