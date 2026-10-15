package com.ecommerce.checkout.infrastructure;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {

    Page<OrderJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<OrderJpaEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
