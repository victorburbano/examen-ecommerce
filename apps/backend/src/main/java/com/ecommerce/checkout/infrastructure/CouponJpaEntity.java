package com.ecommerce.checkout.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Entidad JPA de {@code coupons}. El dominio {@code Coupon} permanece libre de persistencia. */
@Entity
@Table(name = "coupons")
public class CouponJpaEntity {

    @Id
    @Column(length = 50)
    private String code;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percent;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;

    protected CouponJpaEntity() {
        // requerido por JPA
    }

    public CouponJpaEntity(String code, BigDecimal percent, LocalDate validFrom, LocalDate validTo) {
        this.code = code;
        this.percent = percent;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public String getCode() {
        return code;
    }

    public BigDecimal getPercent() {
        return percent;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }
}
