package com.ecommerce.checkout.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.ecommerce.checkout.application.RequestedItem;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Mapeo DTO ↔ dominio: sin lógica de negocio, solo traducción. */
class CheckoutMapperTest {

    private final CheckoutMapper mapper = new CheckoutMapper();

    @Test
    void toCommandMapsItemsAndCoupon() {
        CheckoutRequest request = new CheckoutRequest(
                List.of(new CheckoutRequest.ItemRequest(1L, 2)), "WELCOME2026");

        assertThat(mapper.toCommand(request).items()).containsExactly(new RequestedItem(1L, 2));
        assertThat(mapper.toCommand(request).couponCode()).isEqualTo("WELCOME2026");
    }

    @Test
    void toCommandTreatsAMissingItemsListAsEmpty() {
        assertThat(mapper.toCommand(new CheckoutRequest(null, null)).items()).isEmpty();
    }

    @Test
    void toBreakdownResponseFlattensThePricedCart() {
        BreakdownResponse response = mapper.toBreakdownResponse(CheckoutApiFixtures.pricedCart(true));

        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).unitPrice()).isEqualByComparingTo("80.00");
        assertThat(response.discounts()).hasSize(3);
        assertThat(response.discounts().get(0).type().name()).isEqualTo("CATEGORY");
        assertThat(response.originalSubtotal()).isEqualByComparingTo("130.00");
        assertThat(response.total()).isEqualByComparingTo("98.51");
        assertThat(response.couponCode()).isEqualTo("WELCOME2026");
    }

    @Test
    void toBreakdownResponseReportsNoCouponWhenThereIsNone() {
        assertThat(mapper.toBreakdownResponse(CheckoutApiFixtures.pricedCart(false)).couponCode()).isNull();
    }

    @Test
    void toOrderResponseMapsTheOrder() {
        OrderResponse response = mapper.toOrderResponse(CheckoutApiFixtures.order());

        assertThat(response.orderId()).isEqualTo(CheckoutApiFixtures.ORDER_ID);
        assertThat(response.createdAt()).isEqualTo(CheckoutApiFixtures.CREATED_AT);
        assertThat(response.items()).hasSize(2);
        assertThat(response.discounts()).hasSize(3);
        assertThat(response.total()).isEqualByComparingTo("98.51");
        assertThat(response.couponCode()).isEqualTo("WELCOME2026");
    }

    @Test
    void toPageResponseKeepsThePagingMetadata() {
        PageResponse<OrderResponse> page = mapper.toPageResponse(
                new com.ecommerce.checkout.domain.PageResult<>(List.of(CheckoutApiFixtures.order()), 1, 5, 12));

        assertThat(page.content()).hasSize(1);
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.size()).isEqualTo(5);
        assertThat(page.totalElements()).isEqualTo(12);
        assertThat(page.totalPages()).isEqualTo(3);
    }
}
