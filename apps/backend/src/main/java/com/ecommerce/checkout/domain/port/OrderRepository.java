package com.ecommerce.checkout.domain.port;

import com.ecommerce.checkout.domain.Order;
import com.ecommerce.checkout.domain.PageResult;
import java.util.Optional;
import java.util.UUID;

/** Puerto de persistencia de órdenes. Las órdenes son inmutables: solo se insertan y se leen. */
public interface OrderRepository {

    void save(Order order);

    Optional<Order> findById(UUID id);

    PageResult<Order> findByUserId(Long userId, int page, int size);

    PageResult<Order> findAll(int page, int size);
}
