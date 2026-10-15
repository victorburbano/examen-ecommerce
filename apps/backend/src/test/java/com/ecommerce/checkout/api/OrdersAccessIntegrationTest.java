package com.ecommerce.checkout.api;

import static org.hamcrest.Matchers.hasItem;
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
 * Visibilidad de órdenes por rol y propiedad sobre H2 real: el dueño y un ADMIN leen la orden;
 * otro {@code USER} recibe 404 (nunca 403). No es transaccional, así que limpia en {@code @AfterEach}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrdersAccessIntegrationTest {

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
    void theOwnerAndAnAdminSeeTheOrderButAnotherUserGets404() throws Exception {
        seedProduct(901, "Spec Item A", "TECHNOLOGY", "100.00", 5);
        String ownerToken = login("user@example.com", "User123!");
        String orderId = createOrder(ownerToken);

        // El dueño lee su orden.
        mockMvc.perform(get("/api/orders/" + orderId).header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId));

        // Un ADMIN ve cualquier orden.
        String adminToken = login("admin@example.com", "Admin123!");
        mockMvc.perform(get("/api/orders/" + orderId).header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId));

        // Otro USER recibe 404, no 403: no se revela que la orden existe.
        String otherToken = registerAndLogin("other-" + System.nanoTime() + "@example.com");
        mockMvc.perform(get("/api/orders/" + orderId).header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));

        // Cada uno ve su página: el dueño la suya, el ADMIN todas.
        mockMvc.perform(get("/api/orders").header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].orderId", hasItem(orderId)));
        mockMvc.perform(get("/api/orders").header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].orderId", hasItem(orderId)));
    }

    @Test
    void aUserDoesNotSeeAnotherUsersOrdersInTheList() throws Exception {
        seedProduct(901, "Spec Item A", "TECHNOLOGY", "100.00", 5);
        String ownerToken = login("user@example.com", "User123!");
        String orderId = createOrder(ownerToken);

        String otherToken = registerAndLogin("stranger-" + System.nanoTime() + "@example.com");
        mockMvc.perform(get("/api/orders").header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].orderId", org.hamcrest.Matchers.not(hasItem(orderId))));
    }

    private String createOrder(String token) throws Exception {
        return JsonPath.read(
                mockMvc.perform(post("/api/checkout/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"items\":[{\"productId\":901,\"quantity\":1}]}"))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                "$.orderId");
    }

    private String registerAndLogin(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"User123!\"}"))
                .andExpect(status().isCreated());
        return login(email, "User123!");
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
