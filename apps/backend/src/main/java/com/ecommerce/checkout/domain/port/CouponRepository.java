package com.ecommerce.checkout.domain.port;

import com.ecommerce.checkout.domain.Coupon;
import java.util.Optional;

/** Puerto de lectura de cupones (implementado por el adaptador JPA). */
public interface CouponRepository {

    Optional<Coupon> findByCode(String code);
}
