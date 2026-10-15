package com.ecommerce.catalog.api;

import com.ecommerce.catalog.domain.Product;
import java.util.List;
import org.springframework.stereotype.Component;

/** Traduce el dominio del catálogo a los DTOs de la API (sin lógica de negocio). */
@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.id(), product.name(), product.category(), product.price(), product.stock());
    }

    public List<ProductResponse> toResponse(List<Product> products) {
        return products.stream().map(this::toResponse).toList();
    }
}
