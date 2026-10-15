package com.ecommerce.catalog.infrastructure;

import com.ecommerce.catalog.domain.Product;
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

    private static Product toDomain(ProductJpaEntity entity) {
        return new Product(
                entity.getId(), entity.getName(), entity.getCategory(), entity.getPrice(), entity.getStock());
    }
}
