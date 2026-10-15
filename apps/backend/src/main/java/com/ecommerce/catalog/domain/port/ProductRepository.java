package com.ecommerce.catalog.domain.port;

import com.ecommerce.catalog.domain.Product;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Puerto de persistencia del catálogo (implementado por el adaptador JPA). */
public interface ProductRepository {

    List<Product> findAll();

    Optional<Product> findById(Long id);

    List<Product> findAllById(Collection<Long> ids);

    /**
     * Persiste el producto (el checkout lo usa para decrementar stock). El adaptador fuerza el
     * {@code flush} para que el locking optimista ({@code @Version}) detecte el conflicto dentro
     * de la transacción en curso.
     */
    Product save(Product product);
}
