package com.ecommerce.catalog.infrastructure;

import com.ecommerce.catalog.domain.Product;
import com.ecommerce.catalog.domain.exception.ProductNotFoundException;
import com.ecommerce.catalog.domain.port.ProductRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Adaptador: implementa el puerto de dominio sobre Spring Data JPA. */
@Repository
public class JpaProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    public JpaProductRepositoryAdapter(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAllByOrderByIdAsc().stream()
                .map(JpaProductRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id).map(JpaProductRepositoryAdapter::toDomain);
    }

    @Override
    public List<Product> findAllById(Collection<Long> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(JpaProductRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = jpaRepository
                .findById(product.id())
                .orElseThrow(() -> new ProductNotFoundException(product.id()));
        entity.updateStock(product.stock());
        // saveAndFlush fuerza el UPDATE (con su WHERE version = ?) dentro de la transacción, así
        // que un conflicto de locking optimista se detecta aquí y no al hacer commit.
        return toDomain(jpaRepository.saveAndFlush(entity));
    }

    private static Product toDomain(ProductJpaEntity entity) {
        return new Product(
                entity.getId(), entity.getName(), entity.getCategory(), entity.getPrice(), entity.getStock());
    }
}
