package com.ecommerce.checkout.infrastructure;

import com.ecommerce.checkout.domain.Coupon;
import com.ecommerce.checkout.domain.port.CouponRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Adaptador: implementa el puerto de cupones sobre Spring Data JPA. */
@Repository
public class JpaCouponRepositoryAdapter implements CouponRepository {

    private final CouponJpaRepository jpaRepository;

    public JpaCouponRepositoryAdapter(CouponJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return jpaRepository.findById(code).map(JpaCouponRepositoryAdapter::toDomain);
    }

    private static Coupon toDomain(CouponJpaEntity entity) {
        return new Coupon(entity.getCode(), entity.getPercent(), entity.getValidFrom(), entity.getValidTo());
    }
}
