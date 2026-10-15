package com.ecommerce.checkout.infrastructure;

import com.ecommerce.checkout.domain.Order;
import com.ecommerce.checkout.domain.OrderDiscount;
import com.ecommerce.checkout.domain.OrderItem;
import com.ecommerce.checkout.domain.PageResult;
import com.ecommerce.checkout.domain.port.OrderRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

/** Adaptador: implementa el puerto de órdenes sobre Spring Data JPA. Traduce el {@code Page} de
 * Spring Data al {@code PageResult} de dominio para no filtrar el framework. */
@Repository
public class JpaOrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    public JpaOrderRepositoryAdapter(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Order order) {
        jpaRepository.save(toEntity(order));
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpaRepository.findById(id).map(JpaOrderRepositoryAdapter::toDomain);
    }

    @Override
    public PageResult<Order> findByUserId(Long userId, int page, int size) {
        return toPageResult(jpaRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(page, size)));
    }

    @Override
    public PageResult<Order> findAll(int page, int size) {
        return toPageResult(jpaRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)));
    }

    private static PageResult<Order> toPageResult(Page<OrderJpaEntity> page) {
        List<Order> content = page.getContent().stream().map(JpaOrderRepositoryAdapter::toDomain).toList();
        return new PageResult<>(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    private static Order toDomain(OrderJpaEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(item -> new OrderItem(
                        item.getProductId(), item.getName(), item.getCategory(), item.getUnitPrice(),
                        item.getQuantity(), item.getLineTotal()))
                .toList();
        List<OrderDiscount> discounts = entity.getDiscounts().stream()
                .map(discount -> new OrderDiscount(
                        discount.getType(), discount.getLabel(), discount.getPercent(), discount.getAmount()))
                .toList();
        return new Order(
                entity.getId(),
                entity.getUserId(),
                entity.getCreatedAt(),
                entity.getOriginalSubtotal(),
                entity.getTotalDiscount(),
                entity.getTotal(),
                entity.getEffectiveDiscountPercent(),
                entity.isCapApplied(),
                entity.getCouponCode(),
                items,
                discounts);
    }

    private static OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity(
                order.id(),
                order.userId(),
                order.createdAt(),
                order.originalSubtotal(),
                order.totalDiscount(),
                order.total(),
                order.effectiveDiscountPercent(),
                order.capApplied(),
                order.couponCode());
        order.items().forEach(item -> entity.addItem(new OrderItemJpaEntity(
                entity,
                item.productId(),
                item.name(),
                item.category(),
                item.unitPrice(),
                item.quantity(),
                item.lineTotal())));
        order.discounts().forEach(discount -> entity.addDiscount(new OrderDiscountJpaEntity(
                entity, discount.type(), discount.label(), discount.percent(), discount.amount())));
        return entity;
    }
}
