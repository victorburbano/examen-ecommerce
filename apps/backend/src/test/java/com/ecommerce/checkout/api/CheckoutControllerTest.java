package com.ecommerce.checkout.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.catalog.domain.exception.ProductNotFoundException;
import com.ecommerce.checkout.application.CheckoutCommand;
import com.ecommerce.checkout.application.CheckoutService;
import com.ecommerce.checkout.application.QuoteService;
import com.ecommerce.checkout.domain.exception.CouponExpiredException;
import com.ecommerce.checkout.domain.exception.CouponNotFoundException;
import com.ecommerce.checkout.domain.exception.StockConflictException;
import com.ecommerce.checkout.domain.discount.exception.EmptyCartException;
import com.ecommerce.catalog.domain.exception.InsufficientStockException;
import com.ecommerce.shared.config.AppConfig;
import com.ecommerce.shared.error.ProblemDetailAccessDeniedHandler;
import com.ecommerce.shared.error.ProblemDetailAuthenticationEntryPoint;
import com.ecommerce.shared.security.SecurityConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Slice web del checkout: contrato HTTP, montos como string, errores con {@code code} y
 * autorización (quote público, orders autenticado).
 */
@WebMvcTest(CheckoutController.class)
@Import({
    SecurityConfig.class,
    AppConfig.class,
    CheckoutMapper.class,
    ProblemDetailAuthenticationEntryPoint.class,
    ProblemDetailAccessDeniedHandler.class
})
class CheckoutControllerTest {

    private static final String CART_BODY = """
            {"items":[{"productId":1,"quantity":1},{"productId":2,"quantity":1}],"couponCode":"WELCOME2026"}""";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuoteService quoteService;

    @MockitoBean
    private CheckoutService checkoutService;

    @Test
    void quotesTheCartPubliclyAndSumsTheBreakdown() throws Exception {
        given(quoteService.quote(any(CheckoutCommand.class))).willReturn(CheckoutApiFixtures.pricedCart(true));

        mockMvc.perform(post("/api/checkout/quote").contentType(MediaType.APPLICATION_JSON).content(CART_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value("80.00"))
                .andExpect(jsonPath("$.originalSubtotal").value("130.00"))
                .andExpect(jsonPath("$.discounts.length()").value(3))
                .andExpect(jsonPath("$.discounts[0].type").value("CATEGORY"))
                .andExpect(jsonPath("$.discounts[2].type").value("COUPON"))
                .andExpect(jsonPath("$.totalDiscount").value("31.49"))
                .andExpect(jsonPath("$.effectiveDiscountPercent").value("24.22"))
                .andExpect(jsonPath("$.total").value("98.51"))
                .andExpect(jsonPath("$.capApplied").value(false))
                .andExpect(jsonPath("$.couponCode").value("WELCOME2026"))
                // Los importes son strings JSON, no números (nada de comas flotantes).
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"total\":\"98.51\"")));
    }

    @Test
    void anEmptyQuoteReturns400() throws Exception {
        given(quoteService.quote(any(CheckoutCommand.class))).willThrow(new EmptyCartException());

        mockMvc.perform(post("/api/checkout/quote").contentType(MediaType.APPLICATION_JSON).content("{\"items\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EMPTY_CART"));
    }

    @Test
    void anUnknownProductInAQuoteReturns404() throws Exception {
        given(quoteService.quote(any(CheckoutCommand.class))).willThrow(new ProductNotFoundException(999L));

        mockMvc.perform(post("/api/checkout/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":999,\"quantity\":1}]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void anUnknownCouponReturns422() throws Exception {
        given(quoteService.quote(any(CheckoutCommand.class))).willThrow(new CouponNotFoundException("NOPE"));

        mockMvc.perform(post("/api/checkout/quote").contentType(MediaType.APPLICATION_JSON).content(CART_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("COUPON_NOT_FOUND"));
    }

    @Test
    void anExpiredCouponReturns422() throws Exception {
        given(quoteService.quote(any(CheckoutCommand.class))).willThrow(new CouponExpiredException("EXPIRED2025"));

        mockMvc.perform(post("/api/checkout/quote").contentType(MediaType.APPLICATION_JSON).content(CART_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("COUPON_EXPIRED"));
    }

    @Test
    void aBadQuantityFailsValidation() throws Exception {
        mockMvc.perform(post("/api/checkout/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":1,\"quantity\":0}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void creatingAnOrderRequiresAToken() throws Exception {
        mockMvc.perform(post("/api/checkout/orders").contentType(MediaType.APPLICATION_JSON).content(CART_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anAuthenticatedUserCreatesAnOrder() throws Exception {
        given(checkoutService.checkout(any(), any(CheckoutCommand.class))).willReturn(CheckoutApiFixtures.order());

        mockMvc.perform(post("/api/checkout/orders")
                        .with(jwt().jwt(j -> j.subject("7").claim("roles", List.of("USER")))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CART_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(CheckoutApiFixtures.ORDER_ID.toString()))
                .andExpect(jsonPath("$.createdAt").value("2026-06-15T10:15:30Z"))
                .andExpect(jsonPath("$.total").value("98.51"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.discounts.length()").value(3));
    }

    @Test
    void anInsufficientStockOrderReturns409() throws Exception {
        given(checkoutService.checkout(any(), any(CheckoutCommand.class)))
                .willThrow(new InsufficientStockException(1L, 2, 0));

        mockMvc.perform(post("/api/checkout/orders")
                        .with(jwt().jwt(j -> j.subject("7")).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CART_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.available").value(0));
    }

    @Test
    void aStockConflictAfterRetriesReturns409() throws Exception {
        given(checkoutService.checkout(any(), any(CheckoutCommand.class))).willThrow(new StockConflictException());

        mockMvc.perform(post("/api/checkout/orders")
                        .with(jwt().jwt(j -> j.subject("7")).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CART_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
    }

    @Test
    void anOrderWithATamperedTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/checkout/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CART_BODY))
                .andExpect(status().isUnauthorized());
    }
}
