package com.ecommerce.catalog.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {

    List<ProductJpaEntity> findAllByOrderByIdAsc();
}
