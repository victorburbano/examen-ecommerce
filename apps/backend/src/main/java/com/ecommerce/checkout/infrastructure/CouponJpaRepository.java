package com.ecommerce.checkout.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

interface CouponJpaRepository extends JpaRepository<CouponJpaEntity, String> {
}
