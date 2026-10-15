package com.ecommerce.catalog.api;

import com.ecommerce.catalog.application.ProductQueryService;
import com.ecommerce.catalog.domain.Product;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Catálogo público de solo lectura. Sin lógica de negocio: delega en el caso de uso. */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductQueryService productQueryService;
    private final ProductMapper productMapper;

    public ProductController(ProductQueryService productQueryService, ProductMapper productMapper) {
        this.productQueryService = productQueryService;
        this.productMapper = productMapper;
    }

    @GetMapping
    public List<ProductResponse> list() {
        return productMapper.toResponse(productQueryService.list());
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        Product product = productQueryService.get(id);
        return productMapper.toResponse(product);
    }
}
