package com.ecommerce.catalog.infrastructure;

import com.ecommerce.catalog.domain.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;

/**
 * Entidad JPA de {@code products}. {@code @Version} habilita el locking optimista: el stock
 * nunca baja de 0 porque la regla vive en el dominio (spec 004).
 */
@Entity
@Table(name = "products")
public class ProductJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    @Version
    @Column(nullable = false)
    private Long version;

    protected ProductJpaEntity() {
        // requerido por JPA
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    /** Actualiza el stock (lo usa el checkout). La regla "no bajar de 0" vive en el dominio. */
    public void updateStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("stock must not be negative");
        }
        this.stock = stock;
    }
}
