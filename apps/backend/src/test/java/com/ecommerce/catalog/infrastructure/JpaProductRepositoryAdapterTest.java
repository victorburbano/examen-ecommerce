package com.ecommerce.catalog.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ecommerce.catalog.domain.Category;
import com.ecommerce.catalog.domain.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JpaProductRepositoryAdapterTest {

    @Mock
    private ProductJpaRepository jpaRepository;

    private JpaProductRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaProductRepositoryAdapter(jpaRepository);
    }

    @Test
    void findAllDelegatesToSpringDataAndMapsTheOrder() {
        when(jpaRepository.findAllByOrderByIdAsc())
                .thenReturn(List.of(entity(1L, Category.TECHNOLOGY, "59.90", 10), entity(8L, Category.CLOTHING, "79.00", 0)));

        assertThat(adapter.findAll()).containsExactly(
                new Product(1L, "Auriculares Bluetooth", Category.TECHNOLOGY, new BigDecimal("59.90"), 10),
                new Product(8L, "Auriculares Bluetooth", Category.CLOTHING, new BigDecimal("79.00"), 0));
    }

    @Test
    void findByIdMapsTheEntityToTheDomainOrReturnsEmpty() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity(1L, Category.HOME, "9.99", 2)));
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(1L))
                .contains(new Product(1L, "Auriculares Bluetooth", Category.HOME, new BigDecimal("9.99"), 2));
        assertThat(adapter.findById(99L)).isEmpty();
    }

    @Test
    void findAllByIdMapsTheFoundEntities() {
        when(jpaRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(entity(1L, Category.TECHNOLOGY, "59.90", 10)));

        assertThat(adapter.findAllById(List.of(1L, 2L)))
                .containsExactly(new Product(1L, "Auriculares Bluetooth", Category.TECHNOLOGY, new BigDecimal("59.90"), 10));
    }

    private static ProductJpaEntity entity(Long id, Category category, String price, int stock) {
        ProductJpaEntity entity = new ProductJpaEntity();
        ReflectionTestUtils.setField(entity, "id", id);
        ReflectionTestUtils.setField(entity, "name", "Auriculares Bluetooth");
        ReflectionTestUtils.setField(entity, "category", category);
        ReflectionTestUtils.setField(entity, "price", new BigDecimal(price));
        ReflectionTestUtils.setField(entity, "stock", stock);
        return entity;
    }
}
