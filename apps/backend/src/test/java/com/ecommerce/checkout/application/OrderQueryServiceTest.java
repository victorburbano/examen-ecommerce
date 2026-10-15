package com.ecommerce.checkout.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ecommerce.catalog.domain.Category;
import com.ecommerce.checkout.domain.Order;
import com.ecommerce.checkout.domain.OrderItem;
import com.ecommerce.checkout.domain.PageResult;
import com.ecommerce.checkout.domain.exception.OrderNotFoundException;
import com.ecommerce.checkout.domain.port.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Consulta de órdenes: visibilidad por rol/propiedad y paginación acotada. */
@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest {

    private static final UUID ORDER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderQueryService orderQueryService;

    private static Order orderOwnedBy(long userId) {
        return new Order(
                ORDER_ID,
                userId,
                Instant.parse("2026-06-15T10:00:00Z"),
                new BigDecimal("80.00"),
                BigDecimal.ZERO,
                new BigDecimal("80.00"),
                BigDecimal.ZERO,
                false,
                null,
                List.of(new OrderItem(1L, "Monitor", Category.TECHNOLOGY, new BigDecimal("80.00"), 1, new BigDecimal("80.00"))),
                List.of());
    }

    @Test
    void theOwnerCanReadItsOrder() {
        given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(orderOwnedBy(7L)));

        assertThat(orderQueryService.getForRequester(ORDER_ID, 7L, false).userId()).isEqualTo(7L);
    }

    @Test
    void anAdminCanReadAnyoneSOrder() {
        given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(orderOwnedBy(7L)));

        assertThat(orderQueryService.getForRequester(ORDER_ID, 1L, true).userId()).isEqualTo(7L);
    }

    @Test
    void anotherUserGets404AndNeverSeesThatTheOrderExists() {
        given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(orderOwnedBy(7L)));

        assertThatExceptionOfType(OrderNotFoundException.class)
                .isThrownBy(() -> orderQueryService.getForRequester(ORDER_ID, 99L, false));
    }

    @Test
    void anUnknownOrderIs404() {
        given(orderRepository.findById(ORDER_ID)).willReturn(Optional.empty());

        assertThatExceptionOfType(OrderNotFoundException.class)
                .isThrownBy(() -> orderQueryService.getForRequester(ORDER_ID, 7L, false));
    }

    @Test
    void anAdminListsEveryOrder() {
        given(orderRepository.findAll(0, 10)).willReturn(PageResult.empty(0, 10));

        assertThat(orderQueryService.listForRequester(1L, true, 0, 10).content()).isEmpty();
        verify(orderRepository).findAll(0, 10);
    }

    @Test
    void aUserListsOnlyItsOwnOrders() {
        given(orderRepository.findByUserId(7L, 0, 10)).willReturn(PageResult.empty(0, 10));

        assertThat(orderQueryService.listForRequester(7L, false, 0, 10).content()).isEmpty();
        verify(orderRepository).findByUserId(7L, 0, 10);
    }

    @Test
    void clampsThePagingParametersToSafeBounds() {
        given(orderRepository.findByUserId(7L, 0, 1)).willReturn(PageResult.empty(0, 1));
        given(orderRepository.findByUserId(7L, 3, OrderQueryService.MAX_PAGE_SIZE)).willReturn(PageResult.empty(3, 50));

        orderQueryService.listForRequester(7L, false, -5, 0);
        orderQueryService.listForRequester(7L, false, 3, 9999);

        verify(orderRepository).findByUserId(7L, 0, 1);
        verify(orderRepository).findByUserId(7L, 3, OrderQueryService.MAX_PAGE_SIZE);
    }
}
