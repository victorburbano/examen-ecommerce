package com.ecommerce.checkout.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.checkout.application.OrderQueryService;
import com.ecommerce.checkout.domain.Order;
import com.ecommerce.checkout.domain.PageResult;
import com.ecommerce.checkout.domain.exception.OrderNotFoundException;
import com.ecommerce.shared.config.AppConfig;
import com.ecommerce.shared.error.ProblemDetailAccessDeniedHandler;
import com.ecommerce.shared.error.ProblemDetailAuthenticationEntryPoint;
import com.ecommerce.shared.security.SecurityConfig;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Slice web de órdenes: visibilidad por rol y ownership (otro USER ⇒ 404). */
@WebMvcTest(OrderController.class)
@Import({
    SecurityConfig.class,
    AppConfig.class,
    CheckoutMapper.class,
    ProblemDetailAuthenticationEntryPoint.class,
    ProblemDetailAccessDeniedHandler.class
})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderQueryService orderQueryService;

    @Test
    void listsTheOrdersOfTheCaller() throws Exception {
        given(orderQueryService.listForRequester(anyLong(), anyBoolean(), anyInt(), anyInt()))
                .willReturn(new PageResult<>(List.of(CheckoutApiFixtures.order()), 0, 10, 1));

        mockMvc.perform(get("/api/orders")
                        .with(jwt().jwt(j -> j.subject("7").claim("roles", List.of("USER")))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].orderId").value(CheckoutApiFixtures.ORDER_ID.toString()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(orderQueryService).listForRequester(eq(7L), eq(false), eq(0), eq(10));
    }

    @Test
    void passesTheAdminFlagWhenTheCallerIsAnAdmin() throws Exception {
        given(orderQueryService.listForRequester(anyLong(), anyBoolean(), anyInt(), anyInt()))
                .willReturn(PageResult.empty(0, 10));

        mockMvc.perform(get("/api/orders")
                        .with(jwt().jwt(j -> j.subject("1").claim("roles", List.of("ADMIN")))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());

        verify(orderQueryService).listForRequester(eq(1L), eq(true), eq(0), eq(10));
    }

    @Test
    void getsASingleOrderOfTheCaller() throws Exception {
        given(orderQueryService.getForRequester(eq(CheckoutApiFixtures.ORDER_ID), eq(7L), anyBoolean()))
                .willReturn(CheckoutApiFixtures.order());

        mockMvc.perform(get("/api/orders/" + CheckoutApiFixtures.ORDER_ID)
                        .with(jwt().jwt(j -> j.subject("7").claim("roles", List.of("USER")))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(CheckoutApiFixtures.ORDER_ID.toString()))
                .andExpect(jsonPath("$.total").value("98.51"));
    }

    @Test
    void anotherUsersOrderIs404() throws Exception {
        given(orderQueryService.getForRequester(eq(CheckoutApiFixtures.ORDER_ID), anyLong(), anyBoolean()))
                .willThrow(new OrderNotFoundException(CheckoutApiFixtures.ORDER_ID));

        mockMvc.perform(get("/api/orders/" + CheckoutApiFixtures.ORDER_ID)
                        .with(jwt().jwt(j -> j.subject("99").claim("roles", List.of("USER")))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
    }

    @Test
    void anUnknownOrderIs404() throws Exception {
        UUID unknown = UUID.fromString("99999999-9999-9999-9999-999999999999");
        given(orderQueryService.getForRequester(eq(unknown), anyLong(), anyBoolean()))
                .willThrow(new OrderNotFoundException(unknown));

        mockMvc.perform(get("/api/orders/" + unknown)
                        .with(jwt().jwt(j -> j.subject("7")).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
    }

    @Test
    void listingOrdersRequiresAToken() throws Exception {
        mockMvc.perform(get("/api/orders")).andExpect(status().isUnauthorized());
    }

    @Test
    void gettingAnOrderRequiresAToken() throws Exception {
        mockMvc.perform(get("/api/orders/" + CheckoutApiFixtures.ORDER_ID))
                .andExpect(status().isUnauthorized());
    }
}
