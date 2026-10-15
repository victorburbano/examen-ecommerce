package com.ecommerce.checkout.application;

import com.ecommerce.checkout.domain.Order;
import com.ecommerce.checkout.domain.PageResult;
import com.ecommerce.checkout.domain.exception.OrderNotFoundException;
import com.ecommerce.checkout.domain.port.OrderRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta de órdenes con visibilidad por rol y propiedad: un {@code USER} solo ve las suyas y
 * pedir una ajena devuelve 404 (nunca 403, para no revelar que existe).
 */
@Service
public class OrderQueryService {

    static final int MAX_PAGE_SIZE = 50;

    private final OrderRepository orderRepository;

    public OrderQueryService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public Order getForRequester(UUID orderId, Long userId, boolean admin) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!admin && !order.userId().equals(userId)) {
            throw new OrderNotFoundException(orderId);
        }
        return order;
    }

    @Transactional(readOnly = true)
    public PageResult<Order> listForRequester(Long userId, boolean admin, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return admin
                ? orderRepository.findAll(safePage, safeSize)
                : orderRepository.findByUserId(userId, safePage, safeSize);
    }
}
