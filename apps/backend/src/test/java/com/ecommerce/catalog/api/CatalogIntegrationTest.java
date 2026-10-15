package com.ecommerce.catalog.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Catálogo real sobre H2 + Flyway: el seed de la migración V4 se sirve sin autenticación con
 * precios exactos de 2 decimales.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CatalogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void servesTheEightSeededProductsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8))
                .andExpect(jsonPath("$[0].price").value("59.90"))
                .andExpect(jsonPath("$[0].stock").value(10))
                .andExpect(jsonPath("$[2].name").value("Monitor 27\""))
                .andExpect(jsonPath("$[2].price").value("249.00"))
                .andExpect(jsonPath("$[4].category").value("HOME"))
                .andExpect(jsonPath("$[6].category").value("CLOTHING"))
                // Producto con stock bajo y producto sin stock (para la demo).
                .andExpect(jsonPath("$[5].stock").value(2))
                .andExpect(jsonPath("$[7].stock").value(0));
    }

    @Test
    void servesASingleSeededProduct() throws Exception {
        mockMvc.perform(get("/api/products/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cargador USB-C"))
                .andExpect(jsonPath("$.category").value("TECHNOLOGY"))
                .andExpect(jsonPath("$.price").value("19.90"));
    }

    @Test
    void returns404ForAnUnknownProduct() throws Exception {
        mockMvc.perform(get("/api/products/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }
}
