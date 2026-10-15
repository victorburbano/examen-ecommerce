package com.ecommerce.catalog.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import jakarta.persistence.Version;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

/**
 * Guarda de configuración: el stock se protege con locking optimista. El comportamiento real
 * del {@code @Version} (reintento y conflicto) se prueba en el checkout de la spec 004, donde
 * existe una operación que decrementa stock.
 */
class ProductJpaEntityTest {

    @Test
    void declaresAnOptimisticLockVersionField() throws NoSuchFieldException {
        Field version = ProductJpaEntity.class.getDeclaredField("version");

        assertThat(version.getAnnotation(Version.class)).isNotNull();
    }

    @Test
    void updateStockRejectsANegativeValue() {
        ProductJpaEntity entity = new ProductJpaEntity();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> entity.updateStock(-1))
                .withMessage("stock must not be negative");
    }
}
