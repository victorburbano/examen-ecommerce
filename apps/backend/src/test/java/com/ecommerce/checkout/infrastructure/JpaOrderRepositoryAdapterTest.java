package com.ecommerce.checkout.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ecommerce.catalog.domain.Category;
import com.ecommerce.checkout.domain.Order;
import com.ecommerce.checkout.domain.OrderItem;
import com.ecommerce.checkout.domain.PageResult;
import com.ecommerce.checkout.domain.discount.DiscountStepType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/** Traducción dominio ↔ entidad y paginación de órdenes. */
@ExtendWith(MockitoExtension.class)
class JpaOrderRepositoryAdapterTest {

    private static final UUID ORDER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private OrderJpaRepository jpaRepository;

    private JpaOrderRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaOrderRepositoryAdapter(jpaRepository);
    }

    private static Order order() {
        return new Order(
                ORDER_ID,
                7L,
                Instant.parse("2026-06-15T10:00:00Z"),
                new BigDecimal("80.00"),
                new BigDecimal("8.00"),
                new BigDecimal("72.00"),
                new BigDecimal("10.00"),
                false,
                "WELCOME2026",
                List.of(new OrderItem(1L, "Monitor", Category.TECHNOLOGY, new BigDecimal("80.00"), 1, new BigDecimal("80.00"))),
                List.of(new com.ecommerce.checkout.domain.OrderDiscount(
                        DiscountStepType.CATEGORY, "Tecnología 10 %", new BigDecimal("10.00"), new BigDecimal("8.00"))));
    }

    @Test
    void saveMapsTheDomainOntoTheEntityGraph() {
        adapter.save(order());

        ArgumentCaptor<OrderJpaEntity> captor = ArgumentCaptor.forClass(OrderJpaEntity.class);
        verify(jpaRepository).save(captor.capture());
        OrderJpaEntity entity = captor.getValue();

        assertThat(entity.getId()).isEqualTo(ORDER_ID);
        assertThat(entity.getUserId()).isEqualTo(7L);
        assertThat(entity.getCouponCode()).isEqualTo("WELCOME2026");
        assertThat(entity.isCapApplied()).isFalse();
        assertThat(entity.getItems()).hasSize(1);
        assertThat(entity.getItems().get(0).getProductId()).isEqualTo(1L);
        assertThat(entity.getDiscounts()).hasSize(1);
        assertThat(entity.getDiscounts().get(0).getType()).isEqualTo(DiscountStepType.CATEGORY);
    }

    @Test
    void findByIdMapsTheEntityBackToTheDomain() {
        given(jpaRepository.findById(ORDER_ID)).willReturn(Optional.of(entity()));
        given(jpaRepository.findById(UUID.fromString("44444444-4444-4444-4444-444444444444")))
                .willReturn(Optional.empty());

        assertThat(adapter.findById(ORDER_ID)).contains(order());
        assertThat(adapter.findById(UUID.fromString("44444444-4444-4444-4444-444444444444"))).isEmpty();
    }

    @Test
    void findByUserIdReturnsAPageResultWithTheDomainContent() {
        given(jpaRepository.findByUserIdOrderByCreatedAtDesc(7L, PageRequest.of(0, 10)))
                .willReturn(new PageImpl<>(List.of(entity()), PageRequest.of(0, 10), 1));

        PageResult<Order> page = adapter.findByUserId(7L, 0, 10);

        assertThat(page.content()).containsExactly(order());
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.totalPages()).isEqualTo(1);
    }

    @Test
    void findAllReturnsAPageResultWithTheDomainContent() {
        given(jpaRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 5)))
                .willReturn(new PageImpl<>(List.of(entity()), PageRequest.of(0, 5), 1));

        PageResult<Order> page = adapter.findAll(0, 5);

        assertThat(page.content()).containsExactly(order());
        assertThat(page.number()).isZero();
        assertThat(page.size()).isEqualTo(5);
    }

    private static OrderJpaEntity entity() {
        OrderJpaEntity entity = new OrderJpaEntity(
                ORDER_ID,
                7L,
                Instant.parse("2026-06-15T10:00:00Z"),
                new BigDecimal("80.00"),
                new BigDecimal("8.00"),
                new BigDecimal("72.00"),
                new BigDecimal("10.00"),
                false,
                "WELCOME2026");
        entity.addItem(new OrderItemJpaEntity(
                entity, 1L, "Monitor", Category.TECHNOLOGY, new BigDecimal("80.00"), 1, new BigDecimal("80.00")));
        entity.addDiscount(new OrderDiscountJpaEntity(
                entity, DiscountStepType.CATEGORY, "Tecnología 10 %", new BigDecimal("10.00"), new BigDecimal("8.00")));
        return entity;
    }
}
