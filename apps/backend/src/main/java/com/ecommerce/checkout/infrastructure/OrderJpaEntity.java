package com.ecommerce.checkout.infrastructure;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.BatchSize;

/**
 * Entidad JPA de {@code orders} con sus líneas y descuentos. El id es un UUID que asigna el
 * dominio; las colecciones se cargan en lote ({@link BatchSize}) para evitar N+1 en el listado.
 */
@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "original_subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal originalSubtotal;

    @Column(name = "total_discount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalDiscount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "effective_discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal effectiveDiscountPercent;

    @Column(name = "cap_applied", nullable = false)
    private boolean capApplied;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    @BatchSize(size = 50)
    private List<OrderItemJpaEntity> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    @BatchSize(size = 50)
    private List<OrderDiscountJpaEntity> discounts = new ArrayList<>();

    protected OrderJpaEntity() {
        // requerido por JPA
    }

    public OrderJpaEntity(
            UUID id,
            Long userId,
            Instant createdAt,
            BigDecimal originalSubtotal,
            BigDecimal totalDiscount,
            BigDecimal total,
            BigDecimal effectiveDiscountPercent,
            boolean capApplied,
            String couponCode) {
        this.id = id;
        this.userId = userId;
        this.createdAt = createdAt;
        this.originalSubtotal = originalSubtotal;
        this.totalDiscount = totalDiscount;
        this.total = total;
        this.effectiveDiscountPercent = effectiveDiscountPercent;
        this.capApplied = capApplied;
        this.couponCode = couponCode;
    }

    /** Añade una línea manteniendo la relación bidireccional. */
    public void addItem(OrderItemJpaEntity item) {
        items.add(item);
        item.setOrder(this);
    }

    /** Añade una línea de descuento manteniendo la relación bidireccional. */
    public void addDiscount(OrderDiscountJpaEntity discount) {
        discounts.add(discount);
        discount.setOrder(this);
    }

    public UUID getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public BigDecimal getOriginalSubtotal() {
        return originalSubtotal;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getEffectiveDiscountPercent() {
        return effectiveDiscountPercent;
    }

    public boolean isCapApplied() {
        return capApplied;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public List<OrderItemJpaEntity> getItems() {
        return items;
    }

    public List<OrderDiscountJpaEntity> getDiscounts() {
        return discounts;
    }
}
