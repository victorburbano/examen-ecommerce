package com.ecommerce.checkout.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ecommerce.checkout.domain.Coupon;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JpaCouponRepositoryAdapterTest {

    @Mock
    private CouponJpaRepository jpaRepository;

    private JpaCouponRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaCouponRepositoryAdapter(jpaRepository);
    }

    @Test
    void mapsTheEntityToTheDomainOrReturnsEmpty() {
        CouponJpaEntity entity = new CouponJpaEntity(
                "WELCOME2026", new BigDecimal("15.00"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        when(jpaRepository.findById("WELCOME2026")).thenReturn(Optional.of(entity));
        when(jpaRepository.findById("NOPE")).thenReturn(Optional.empty());

        assertThat(adapter.findByCode("WELCOME2026")).contains(new Coupon(
                "WELCOME2026", new BigDecimal("15.00"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));
        assertThat(adapter.findByCode("NOPE")).isEmpty();
    }
}
