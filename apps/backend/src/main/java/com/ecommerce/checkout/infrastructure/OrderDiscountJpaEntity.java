package com.ecommerce.checkout.infrastructure;

import com.ecommerce.checkout.domain.discount.DiscountStepType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** Entidad JPA de {@code order_discounts}: una línea del desglose, con {@code percent} nullable. */
@Entity
@Table(name = "order_discounts")
public class OrderDiscountJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountStepType type;

    @Column(nullable = false, length = 120)
    private String label;

    @Column(precision = 5, scale = 2)
    private BigDecimal percent;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    protected OrderDiscountJpaEntity() {
        // requerido por JPA
    }

    public OrderDiscountJpaEntity(
            OrderJpaEntity order, DiscountStepType type, String label, BigDecimal percent, BigDecimal amount) {
        this.order = order;
        this.type = type;
        this.label = label;
        this.percent = percent;
        this.amount = amount;
    }

    void setOrder(OrderJpaEntity order) {
        this.order = order;
    }

    public DiscountStepType getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getPercent() {
        return percent;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
