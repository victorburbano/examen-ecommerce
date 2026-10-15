package com.ecommerce.catalog.api;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.catalog.application.ProductQueryService;
import com.ecommerce.catalog.domain.Category;
import com.ecommerce.catalog.domain.Product;
import com.ecommerce.catalog.domain.exception.ProductNotFoundException;
import com.ecommerce.shared.config.AppConfig;
import com.ecommerce.shared.error.ProblemDetailAccessDeniedHandler;
import com.ecommerce.shared.error.ProblemDetailAuthenticationEntryPoint;
import com.ecommerce.shared.security.SecurityConfig;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Slice web del catálogo: contrato HTTP, precio como string decimal y 404. El endpoint es
 * público, así que no requiere token.
 */
@WebMvcTest(ProductController.class)
@Import({
    SecurityConfig.class,
    AppConfig.class,
    ProductMapper.class,
    ProblemDetailAuthenticationEntryPoint.class,
    ProblemDetailAccessDeniedHandler.class
})
class ProductControllerTest {

    private static final Product HEADPHONES =
            new Product(1L, "Auriculares Bluetooth", Category.TECHNOLOGY, new BigDecimal("59.90"), 10);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductQueryService productQueryService;

    @Test
    void listsTheCatalogWithThePriceAsAString() throws Exception {
        given(productQueryService.list()).willReturn(List.of(HEADPHONES));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Auriculares Bluetooth"))
                .andExpect(jsonPath("$[0].category").value("TECHNOLOGY"))
                .andExpect(jsonPath("$[0].stock").value(10))
                .andExpect(jsonPath("$[0].price").value("59.90"))
                // El precio es un string JSON, no un número (nada de comas flotantes).
                .andExpect(content().string(containsString("\"price\":\"59.90\"")));
    }

    @Test
    void getsAProductById() throws Exception {
        given(productQueryService.get(1L)).willReturn(HEADPHONES);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.price").value("59.90"));
    }

    @Test
    void returns404WhenTheProductDoesNotExist() throws Exception {
        given(productQueryService.get(999L)).willThrow(new ProductNotFoundException(999L));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }
}
