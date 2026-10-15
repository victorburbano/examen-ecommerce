package com.ecommerce.checkout.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Checkout real sobre H2 + Flyway: cotización pública, alta de orden con decremento de stock,
 * rollback ante stock insuficiente, cupones y tope. No es transaccional (necesita commits reales
 * para probar el rollback), así que usa productos propios (ids 901-903) y limpia en {@code @AfterEach}
 * para no romper las aserciones del seed de otras suites.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CheckoutIntegrationTest {

    private static final String CART_WITH_WELCOME =
            "{\"items\":[{\"productId\":1,\"quantity\":1},{\"productId\":5,\"quantity\":1}],\"couponCode\":\"WELCOME2026\"}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM orders");
        jdbcTemplate.update("DELETE FROM products WHERE id IN (901, 902, 903)");
    }

    @Test
    void quotesTheSeedCartPubliclyWithExactTotals() throws Exception {
        mockMvc.perform(post("/api/checkout/quote").contentType(MediaType.APPLICATION_JSON).content(CART_WITH_WELCOME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalSubtotal").value("94.40"))
                .andExpect(jsonPath("$.discounts[0].type").value("CATEGORY"))
                .andExpect(jsonPath("$.discounts[0].amount").value("5.99"))
                // El carrito queda por debajo de 100.00, así que no hay paso de volumen.
                .andExpect(jsonPath("$.discounts[1].type").value("COUPON"))
                .andExpect(jsonPath("$.discounts[1].amount").value("13.26"))
                .andExpect(jsonPath("$.totalDiscount").value("19.25"))
                .andExpect(jsonPath("$.effectiveDiscountPercent").value("20.39"))
                .andExpect(jsonPath("$.total").value("75.15"))
                .andExpect(jsonPath("$.capApplied").value(false));
    }

    @Test
    void quotesMega40WithTheCapApplied() throws Exception {
        mockMvc.perform(post("/api/checkout/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":3,\"quantity\":1}],\"couponCode\":\"MEGA40\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capApplied").value(true))
                .andExpect(jsonPath("$.effectiveDiscountPercent").value("35.00"))
                .andExpect(jsonPath("$.total").value("161.85"))
                .andExpect(jsonPath("$.discounts[3].type").value("CAP_ADJUSTMENT"))
                .andExpect(jsonPath("$.discounts[3].amount").value("-34.12"));
    }

    @Test
    void ignoresClientProvidedTotals() throws Exception {
        // El cliente intenta colar un total: el servidor lo ignora y recalcula.
        mockMvc.perform(post("/api/checkout/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":3,\"quantity\":1}],\"total\":\"0.01\",\"couponCode\":\"MEGA40\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value("161.85"));
    }

    @Test
    void createsAnOrderDecrementsStockAndPersistsTheBreakdown() throws Exception {
        seedProduct(901, "Spec Item A", "TECHNOLOGY", "100.00", 5);
        seedProduct(903, "Spec Item C", "HOME", "50.00", 5);

        String body = "{\"items\":[{\"productId\":901,\"quantity\":1},{\"productId\":903,\"quantity\":1}]}";
        String token = login("user@example.com", "User123!");

        String response = mockMvc.perform(post("/api/checkout/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalSubtotal").value("150.00"))
                .andExpect(jsonPath("$.totalDiscount").value("17.00"))
                .andExpect(jsonPath("$.total").value("133.00"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(stockOf(901)).isEqualTo(4);
        assertThat(stockOf(903)).isEqualTo(4);

        String orderId = JsonPath.read(response, "$.orderId");
        mockMvc.perform(get("/api/orders/" + orderId).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.total").value("133.00"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.discounts.length()").value(2));
    }

    @Test
    void rollsBackTheWholeOrderWhenOneLineHasNoStock() throws Exception {
        seedProduct(901, "Spec Item A", "TECHNOLOGY", "100.00", 5);
        seedProduct(902, "Spec Item B", "HOME", "50.00", 0);

        String token = login("user@example.com", "User123!");

        mockMvc.perform(post("/api/checkout/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":901,\"quantity\":1},{\"productId\":902,\"quantity\":1}]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));

        // La primera línea (ya descontada y flusheada) vuelve atrás con el rollback.
        assertThat(stockOf(901)).isEqualTo(5);
        assertThat(orderCount()).isZero();
    }

    @Test
    void rejectsAnUnknownProductInAQuote() throws Exception {
        mockMvc.perform(post("/api/checkout/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":9999,\"quantity\":1}]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void rejectsAnUnknownCoupon() throws Exception {
        mockMvc.perform(post("/api/checkout/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":1,\"quantity\":1}],\"couponCode\":\"NOPE\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("COUPON_NOT_FOUND"));
    }

    @Test
    void rejectsAnExpiredCoupon() throws Exception {
        mockMvc.perform(post("/api/checkout/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":1,\"quantity\":1}],\"couponCode\":\"EXPIRED2025\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("COUPON_EXPIRED"));
    }

    @Test
    void rejectsAnEmptyOrCorruptCart() throws Exception {
        mockMvc.perform(post("/api/checkout/quote").contentType(MediaType.APPLICATION_JSON).content("{\"items\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EMPTY_CART"));

        mockMvc.perform(post("/api/checkout/quote").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EMPTY_CART"));

        mockMvc.perform(post("/api/checkout/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":1,\"quantity\":1},{\"productId\":1,\"quantity\":1}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CART"));

        mockMvc.perform(post("/api/checkout/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":1,\"quantity\":-1}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void creatingAnOrderWithoutATokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/checkout/orders").contentType(MediaType.APPLICATION_JSON).content(CART_WITH_WELCOME))
                .andExpect(status().isUnauthorized());
    }

    private void seedProduct(long id, String name, String category, String price, int stock) {
        jdbcTemplate.update(
                "MERGE INTO products (id, name, category, price, stock, version) KEY(id) VALUES (?, ?, ?, ?, ?, 0)",
                id,
                name,
                category,
                new java.math.BigDecimal(price),
                stock);
    }

    private int stockOf(long productId) {
        Integer stock = jdbcTemplate.queryForObject("SELECT stock FROM products WHERE id = ?", Integer.class, productId);
        assertThat(stock).isNotNull();
        return stock;
    }

    private int orderCount() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
        assertThat(count).isNotNull();
        return count;
    }

    private String login(String email, String password) throws Exception {
        return JsonPath.read(
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                "$.accessToken");
    }
}
