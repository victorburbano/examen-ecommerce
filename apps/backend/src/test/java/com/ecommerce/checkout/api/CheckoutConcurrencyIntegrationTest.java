package com.ecommerce.checkout.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Dos compradores simultáneos por la última unidad: el locking optimista ({@code @Version}) +
 * reintento garantizan que se cree **exactamente una** orden; el otro recibe 409 y el stock acaba
 * en 0. No es transaccional (necesita commits reales entre hilos).
 */
@SpringBootTest
@AutoConfigureMockMvc
class CheckoutConcurrencyIntegrationTest {

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
    void exactlyOneOfTwoConcurrentBuyersGetsTheLastUnit() throws Exception {
        jdbcTemplate.update(
                "MERGE INTO products (id, name, category, price, stock, version) KEY(id) VALUES (901, 'Última unidad', 'TECHNOLOGY', ?, 1, 0)",
                new BigDecimal("100.00"));
        String token = login("user@example.com", "User123!");
        String body = "{\"items\":[{\"productId\":901,\"quantity\":1}]}";

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Callable<Integer>> tasks = IntStream.range(0, 2)
                    .<Callable<Integer>>mapToObj(index -> () -> {
                        start.await();
                        MvcResult result = mockMvc.perform(post("/api/checkout/orders")
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                .andReturn();
                        return result.getResponse().getStatus();
                    })
                    .toList();

            List<Future<Integer>> futures = tasks.stream().map(pool::submit).toList();
            start.countDown();

            List<Integer> statuses = futures.stream().map(CheckoutConcurrencyIntegrationTest::get).sorted().toList();

            assertThat(statuses).containsExactly(201, 409);
            assertThat(stockOf(901)).isZero();
            assertThat(orderCount()).isEqualTo(1);
        } finally {
            pool.shutdownNow();
            pool.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private static int get(Future<Integer> future) {
        try {
            return future.get(30, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
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
